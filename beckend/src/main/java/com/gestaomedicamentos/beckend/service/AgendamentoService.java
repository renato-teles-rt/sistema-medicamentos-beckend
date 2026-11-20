package com.gestaomedicamentos.beckend.service;

import com.gestaomedicamentos.beckend.model.Agendamento;
import com.gestaomedicamentos.beckend.repository.AgendamentoRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
    }

    // Lista todos os agendamentos ativos ordenados do mais cedo para o mais tarde
    public List<Agendamento> listarAtivosOrdenados() {
        return agendamentoRepository.findByAtivoTrueOrderByHorarioAsc();
    }

    // Cria um novo horário de medicação validando os campos obrigatórios
    public Agendamento criarAgendamento(Agendamento agendamento) {
        if (agendamento.getNomeMedicamento() == null || agendamento.getNomeMedicamento().isBlank()) {
            throw new IllegalArgumentException("O nome do medicamento é obrigatório.");
        }
        if (agendamento.getHorario() == null) {
            throw new IllegalArgumentException("O horário da dose é obrigatório.");
        }
        if (agendamento.getAtivo() == null) {
            agendamento.setAtivo(true);
        }
        return agendamentoRepository.save(agendamento);
    }

    // Busca um agendamento pelo ID ou avisa caso não exista
    public Agendamento buscarPorId(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agendamento não encontrado para o ID: " + id));
    }

    // Atualiza nome, dosagem, horário ou status ativo de um agendamento existente
    public Agendamento atualizarAgendamento(Long id, Agendamento dadosAtualizados) {
        Agendamento existente = buscarPorId(id);

        if (dadosAtualizados.getNomeMedicamento() != null && !dadosAtualizados.getNomeMedicamento().isBlank()) {
            existente.setNomeMedicamento(dadosAtualizados.getNomeMedicamento());
        }
        if (dadosAtualizados.getDosagem() != null) {
            existente.setDosagem(dadosAtualizados.getDosagem());
        }
        if (dadosAtualizados.getHorario() != null) {
            existente.setHorario(dadosAtualizados.getHorario());
        }
        if (dadosAtualizados.getAtivo() != null) {
            existente.setAtivo(dadosAtualizados.getAtivo());
        }
        if (dadosAtualizados.getEstoqueId() != null) {
            existente.setEstoqueId(dadosAtualizados.getEstoqueId());
        }

        return agendamentoRepository.save(existente);
    }

    // Desativa o agendamento sem apagar o histórico passado de doses
    public void desativarAgendamento(Long id) {
        Agendamento existente = buscarPorId(id);
        existente.setAtivo(false);
        agendamentoRepository.save(existente);
    }
}