package com.gestaomedicamentos.beckend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vinculos_familiares")
public class VinculoFamiliar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liga este vínculo ao usuário que é o Cuidador/Familiar
    @ManyToOne
    @JoinColumn(name = "cuidador_id", nullable = false)
    private Usuario cuidador;

    // Liga este vínculo ao usuário que é o Paciente 50+
    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Usuario paciente;

    @Column(name = "data_vinculo", nullable = false)
    private LocalDateTime dataVinculo;

    @Column(nullable = false)
    private Boolean ativo = true;

    // Preenche automaticamente a data e hora exata em que o vínculo foi criado
    @PrePersist
    public void aoCriar() {
        if (this.dataVinculo == null) {
            this.dataVinculo = LocalDateTime.now();
        }
    }

    // Construtor vazio obrigatório para o Hibernate/JPA
    public VinculoFamiliar() {
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getCuidador() {
        return cuidador;
    }

    public void setCuidador(Usuario cuidador) {
        this.cuidador = cuidador;
    }

    public Usuario getPaciente() {
        return paciente;
    }

    public void setPaciente(Usuario paciente) {
        this.paciente = paciente;
    }

    public LocalDateTime getDataVinculo() {
        return dataVinculo;
    }

    public void setDataVinculo(LocalDateTime dataVinculo) {
        this.dataVinculo = dataVinculo;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}