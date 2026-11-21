package com.gestaomedicamentos.beckend.service;

import com.gestaomedicamentos.beckend.model.Agendamento;
import com.gestaomedicamentos.beckend.repository.AgendamentoRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class GoogleAgendaService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleAgendaService.class);
    private static final String GOOGLE_CALENDAR_EVENTS_URL = "https://www.googleapis.com/calendar/v3/calendars/primary/events";

    private final AgendamentoRepository agendamentoRepository;
    private final HttpClient httpClient;

    public GoogleAgendaService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        // RNF03: Limite máximo de 2 segundos para comunicação com a API externa
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    /**
     * RNF03 e RN10: Espelha o compromisso e o pré-aviso de 5 minutos no Google Agenda
     * de forma assíncrona (@Async), sem atrasar o salvamento local no MySQL.
     */
    @Async
    public void sincronizarAgendamentoAsync(Long agendamentoId, String googleAccessToken) {
        try {
            Agendamento agendamento = agendamentoRepository.findById(agendamentoId).orElse(null);
            if (agendamento == null || !Boolean.TRUE.equals(agendamento.getAtivo())) {
                return;
            }

            // Se o usuário ainda não enviou token OAuth2 do Google, registra sincronização pendente sem travar o app
            if (googleAccessToken == null || googleAccessToken.isBlank()) {
                if (agendamento.getGoogleEventId() == null) {
                    agendamento.setGoogleEventId("LOCAL-PENDENTE-" + UUID.randomUUID().toString().substring(0, 8));
                    agendamentoRepository.save(agendamento);
                }
                logger.info("RNF03/RN10: Agendamento {} salvo localmente; aguardando token Google Agenda.", agendamentoId);
                return;
            }

            String payloadJson = montarJsonEventoGoogle(agendamento);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GOOGLE_CALENDAR_EVENTS_URL))
                    .timeout(Duration.ofSeconds(2)) // RNF03: Timeout estrito de 2 segundos
                    .header("Authorization", "Bearer " + googleAccessToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String eventId = extrairIdEvento(response.body());
                agendamento.setGoogleEventId(eventId);
                agendamentoRepository.save(agendamento);
                logger.info("RNF03/RN10: Agendamento {} espelhado no Google Agenda (ID: {}).", agendamentoId, eventId);
            } else {
                logger.warn("RNF03: API Google Agenda respondeu status {} para agendamento {}. Alarme local mantido.",
                        response.statusCode(), agendamentoId);
            }
        } catch (Exception e) {
            // RNF03: Falha ou oscilação externa NUNCA derruba o agendamento local
            logger.warn("RNF03: Oscilação na API do Google Agenda para agendamento {}: {}. Alarme local garantido.",
                    agendamentoId, e.getMessage());
        }
    }

    // Monta o JSON padrão da API Google Calendar com lembrete de 5 minutos antes (RN10)
    private String montarJsonEventoGoogle(Agendamento agendamento) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = LocalDateTime.of(hoje, agendamento.getHorario());
        LocalDateTime fim = inicio.plusMinutes(15);
        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        String titulo = "Medicamento: " + agendamento.getNomeMedicamento()
                + (agendamento.getDosagem() != null ? " (" + agendamento.getDosagem() + ")" : "");

        return """
                {
                  "summary": "%s",
                  "description": "Lembrete automático - Sistema de Gestão de Medicações 50+ (Pré-aviso: %s)",
                  "start": { "dateTime": "%s-03:00", "timeZone": "America/Sao_Paulo" },
                  "end": { "dateTime": "%s-03:00", "timeZone": "America/Sao_Paulo" },
                  "reminders": {
                    "useDefault": false,
                    "overrides": [
                      { "method": "popup", "minutes": 5 },
                      { "method": "popup", "minutes": 0 }
                    ]
                  }
                }
                """.formatted(
                titulo.replace("\"", "'"),
                agendamento.getHorarioPreAviso(),
                inicio.format(iso),
                fim.format(iso)
        );
    }

    private String extrairIdEvento(String responseBody) {
        int idx = responseBody.indexOf("\"id\":");
        if (idx != -1) {
            int startQuote = responseBody.indexOf("\"", idx + 5);
            int endQuote = responseBody.indexOf("\"", startQuote + 1);
            if (startQuote != -1 && endQuote != -1) {
                return responseBody.substring(startQuote + 1, endQuote);
            }
        }
        return "GOOGLE-SYNC-" + UUID.randomUUID().toString().substring(0, 8);
    }
}