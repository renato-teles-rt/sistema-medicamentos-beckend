package com.gestaomedicamentos.beckend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_dose")
public class RegistroDose {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liga este registro ao horário programado na tabela Agendamento
    @ManyToOne
    @JoinColumn(name = "agendamento_id", nullable = false)
    private Agendamento agendamento;

    @Column(name = "data_hora_prevista", nullable = false)
    private LocalDateTime dataHoraPrevista;

    @Column(name = "data_hora_tomada")
    private LocalDateTime dataHoraTomada;

    // Valores esperados: "PENDENTE", "TOMADO", "ATRASADO" ou "PULADO"
    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 255)
    private String observacao;

    public RegistroDose() {
    }

    public RegistroDose(Agendamento agendamento, LocalDateTime dataHoraPrevista, LocalDateTime dataHoraTomada, String status, String observacao) {
        this.agendamento = agendamento;
        this.dataHoraPrevista = dataHoraPrevista;
        this.dataHoraTomada = dataHoraTomada;
        this.status = status;
        this.observacao = observacao;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Agendamento getAgendamento() {
        return agendamento;
    }

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;
    }

    public LocalDateTime getDataHoraPrevista() {
        return dataHoraPrevista;
    }

    public void setDataHoraPrevista(LocalDateTime dataHoraPrevista) {
        this.dataHoraPrevista = dataHoraPrevista;
    }

    public LocalDateTime getDataHoraTomada() {
        return dataHoraTomada;
    }

    public void setDataHoraTomada(LocalDateTime dataHoraTomada) {
        this.dataHoraTomada = dataHoraTomada;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}