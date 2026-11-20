package com.gestaomedicamentos.beckend.repository;

import com.gestaomedicamentos.beckend.model.RegistroDose;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistroDoseRepository extends JpaRepository<RegistroDose, Long> {

    // Busca todas as doses previstas dentro de um período (ex: do início ao fim do dia de hoje)
    List<RegistroDose> findByDataHoraPrevistaBetweenOrderByDataHoraPrevistaAsc(LocalDateTime inicio, LocalDateTime fim);

    // Busca doses por status (ex: "PENDENTE", "TOMADO", "ATRASADO") ordenadas pelo horário
    List<RegistroDose> findByStatusOrderByDataHoraPrevistaAsc(String status);

    // Busca o histórico completo de doses de um agendamento específico (do mais recente para o mais antigo)
    List<RegistroDose> findByAgendamentoIdOrderByDataHoraPrevistaDesc(Long agendamentoId);
}