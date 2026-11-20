package com.gestaomedicamentos.beckend.repository;

import com.gestaomedicamentos.beckend.model.Agendamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Busca apenas os agendamentos ativos e já organiza do horário mais cedo para o mais tarde
    List<Agendamento> findByAtivoTrueOrderByHorarioAsc();

    // Busca todos os agendamentos ligados a um medicamento específico do Estoque (do Jason)
    List<Agendamento> findByEstoqueId(Long estoqueId);
}