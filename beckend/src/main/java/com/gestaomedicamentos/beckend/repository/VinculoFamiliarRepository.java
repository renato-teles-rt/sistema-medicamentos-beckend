package com.gestaomedicamentos.beckend.repository;

import com.gestaomedicamentos.beckend.model.VinculoFamiliar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VinculoFamiliarRepository extends JpaRepository<VinculoFamiliar, Long> {

    // Lista todos os pacientes ativos acompanhados por um cuidador
    List<VinculoFamiliar> findByCuidadorIdAndAtivoTrue(Long cuidadorId);

    // Lista todos os cuidadores ativos de um paciente
    List<VinculoFamiliar> findByPacienteIdAndAtivoTrue(Long pacienteId);

    // Verifica se já existe um vínculo entre esse cuidador e esse paciente para não duplicar
    Optional<VinculoFamiliar> findByCuidadorIdAndPacienteId(Long cuidadorId, Long pacienteId);
}