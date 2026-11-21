package com.gestaomedicamentos.beckend.repository;

import com.gestaomedicamentos.beckend.model.Agendamento;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Busca apenas os agendamentos ativos e já organiza do horário mais cedo para o mais tarde
    List<Agendamento> findByAtivoTrueOrderByHorarioAsc();

    // Busca todos os agendamentos ligados a um medicamento específico do Estoque
    List<Agendamento> findByEstoqueId(Long estoqueId);

    // RN03 / RN09: Verifica se já existe agendamento ativo no mesmo horário (ao criar)
    boolean existsByHorarioAndAtivoTrue(LocalTime horario);

    // RN03 / RN09: Verifica se já existe outro agendamento ativo no mesmo horário (ao editar)
    boolean existsByHorarioAndAtivoTrueAndIdNot(LocalTime horario, Long id);
}