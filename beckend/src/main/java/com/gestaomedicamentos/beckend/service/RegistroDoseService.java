package com.gestaomedicamentos.beckend.service;

import com.gestaomedicamentos.beckend.model.Agendamento;
import com.gestaomedicamentos.beckend.model.RegistroDose;
import com.gestaomedicamentos.beckend.repository.AgendamentoRepository;
import com.gestaomedicamentos.beckend.repository.RegistroDoseRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RegistroDoseService {

    private final RegistroDoseRepository registroDoseRepository;
    private final AgendamentoRepository agendamentoRepository;

    public RegistroDoseService(RegistroDoseRepository registroDoseRepository, AgendamentoRepository agendamentoRepository) {
        this.registroDoseRepository = registroDoseRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    // Lista as doses de uma data (ex: hoje). Se ainda não foram geradas, cria automaticamente a partir dos agendamentos ativos
    public List<RegistroDose> listarOuGerarDosesDoDia(LocalDate data) {
        LocalDateTime inicioDoDia = data.atStartOfDay();
        LocalDateTime fimDoDia = data.atTime(LocalTime.MAX);

        List<RegistroDose> dosesDoDia = registroDoseRepository
                .findByDataHoraPrevistaBetweenOrderByDataHoraPrevistaAsc(inicioDoDia, fimDoDia);

        if (dosesDoDia.isEmpty()) {
            List<Agendamento> agendamentosAtivos = agendamentoRepository.findByAtivoTrueOrderByHorarioAsc();
            List<RegistroDose> novasDoses = new ArrayList<>();

            for (Agendamento agendamento : agendamentosAtivos) {
                LocalDateTime dataHoraPrevista = LocalDateTime.of(data, agendamento.getHorario());
                RegistroDose novaDose = new RegistroDose(
                        agendamento,
                        dataHoraPrevista,
                        null,
                        "PENDENTE",
                        null
                );
                novasDoses.add(registroDoseRepository.save(novaDose));
            }
            return novasDoses;
        }

        return dosesDoDia;
    }

    // Marca uma dose como TOMADO e grava a data e hora exatas da confirmação
    public RegistroDose confirmarDoseTomada(Long registroDoseId, String observacao) {
        RegistroDose dose = buscarPorId(registroDoseId);
        dose.setStatus("TOMADO");
        dose.setDataHoraTomada(LocalDateTime.now());

        if (observacao != null && !observacao.isBlank()) {
            dose.setObservacao(observacao);
        }

        return registroDoseRepository.save(dose);
    }

    // Marca uma dose como PULADO e registra uma observação opcional para o cuidador
    public RegistroDose marcarDosePulada(Long registroDoseId, String observacao) {
        RegistroDose dose = buscarPorId(registroDoseId);
        dose.setStatus("PULADO");

        if (observacao != null && !observacao.isBlank()) {
            dose.setObservacao(observacao);
        }

        return registroDoseRepository.save(dose);
    }

    // Busca o histórico completo de doses de um medicamento agendado
    public List<RegistroDose> listarHistoricoPorAgendamento(Long agendamentoId) {
        return registroDoseRepository.findByAgendamentoIdOrderByDataHoraPrevistaDesc(agendamentoId);
    }

    // Método auxiliar interno para localizar um registro pelo ID
    private RegistroDose buscarPorId(Long id) {
        return registroDoseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Registro de dose não encontrado para o ID: " + id));
    }
}