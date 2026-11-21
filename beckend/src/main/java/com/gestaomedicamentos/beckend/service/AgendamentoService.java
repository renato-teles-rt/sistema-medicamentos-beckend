package com.gestaomedicamentos.beckend.service;

import com.gestaomedicamentos.beckend.model.Agendamento;
import com.gestaomedicamentos.beckend.repository.AgendamentoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final GoogleAgendaService googleAgendaService;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, GoogleAgendaService googleAgendaService) {
        this.agendamentoRepository = agendamentoRepository;
        this.googleAgendaService = googleAgendaService;
    }

    // Lista todos os agendamentos ativos ordenados do mais cedo para o mais tarde
    public List<Agendamento> listarAtivosOrdenados() {
        return agendamentoRepository.findByAtivoTrueOrderByHorarioAsc();
    }

    // Sobrecarga padrão (sem token do Google)
    public Agendamento criarAgendamento(Agendamento agendamento) {
        return criarAgendamento(agendamento, null);
    }

    // Cria um novo horário validando campos, conflito (RN03/RN09) e disparando Google Agenda (RNF03/RN10)
    public Agendamento criarAgendamento(Agendamento agendamento, String googleAccessToken) {
        if (agendamento.getNomeMedicamento() == null || agendamento.getNomeMedicamento().isBlank()) {
            throw new IllegalArgumentException("O nome do medicamento é obrigatório.");
        }
        if (agendamento.getHorario() == null) {
            throw new IllegalArgumentException("O horário da dose é obrigatório.");
        }

        // RN03 / RN09: Verifica conflito de horário entre agendamentos ativos
        if (agendamentoRepository.existsByHorarioAndAtivoTrue(agendamento.getHorario())) {
            throw new IllegalStateException(
                    "Conflito de horário: já existe um medicamento ativo agendado para " + agendamento.getHorario());
        }

        if (agendamento.getAtivo() == null) {
            agendamento.setAtivo(true);
        }

        // Garante o cálculo do pré-aviso de 5 minutos (RN10) e salva no MySQL (< 1s)
        agendamento.calcularPreAviso();
        Agendamento salvo = agendamentoRepository.save(agendamento);

        // RNF03 / RN10: Dispara sincronização assíncrona com o Google Agenda
        if (Boolean.TRUE.equals(salvo.getAtivo())) {
            googleAgendaService.sincronizarAgendamentoAsync(salvo.getId(), googleAccessToken);
        }

        return salvo;
    }

    // Busca um agendamento pelo ID ou avisa caso não exista
    public Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado para o ID: " + id));
    }

    // Sobrecarga padrão (sem token do Google)
    public Agendamento atualizarAgendamento(Long id, Agendamento dadosAtualizados) {
        return atualizarAgendamento(id, dadosAtualizados, null);
    }

    // Atualiza agendamento verificando conflito de horário (RN03/RN09) e ressincronizando (RNF03/RN10)
    public Agendamento atualizarAgendamento(Long id, Agendamento dadosAtualizados, String googleAccessToken) {
        Agendamento existente = buscarPorId(id);

        if (dadosAtualizados.getHorario() != null) {
            // RN03 / RN09: Verifica se outro agendamento ativo já ocupa o novo horário
            if (agendamentoRepository.existsByHorarioAndAtivoTrueAndIdNot(dadosAtualizados.getHorario(), id)) {
                throw new IllegalStateException(
                        "Conflito de horário: já existe outro medicamento ativo agendado para "
                                + dadosAtualizados.getHorario());
            }
            existente.setHorario(dadosAtualizados.getHorario());
        }

        if (dadosAtualizados.getNomeMedicamento() != null && !dadosAtualizados.getNomeMedicamento().isBlank()) {
            existente.setNomeMedicamento(dadosAtualizados.getNomeMedicamento());
        }
        if (dadosAtualizados.getDosagem() != null) {
            existente.setDosagem(dadosAtualizados.getDosagem());
        }
        if (dadosAtualizados.getAtivo() != null) {
            existente.setAtivo(dadosAtualizados.getAtivo());
        }
        if (dadosAtualizados.getEstoqueId() != null) {
            existente.setEstoqueId(dadosAtualizados.getEstoqueId());
        }

        existente.calcularPreAviso();
        Agendamento atualizado = agendamentoRepository.save(existente);

        // RNF03 / RN10: Espelha atualização no Google Agenda de forma assíncrona
        if (Boolean.TRUE.equals(atualizado.getAtivo())) {
            googleAgendaService.sincronizarAgendamentoAsync(atualizado.getId(), googleAccessToken);
        }

        return atualizado;
    }

    // Desativa o agendamento sem apagar o histórico passado de doses
    public void desativarAgendamento(Long id) {
        Agendamento existente = buscarPorId(id);
        existente.setAtivo(false);
        agendamentoRepository.save(existente);
    }
}