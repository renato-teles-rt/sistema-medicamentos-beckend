package com.gestaomedicamentos.beckend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "agendamento")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "estoque_id")
    private Long estoqueId;

    @Column(name = "nome_medicamento", nullable = false, length = 120)
    private String nomeMedicamento;

    @Column(length = 60)
    private String dosagem;

    @Column(nullable = false)
    private LocalTime horario;

    // RN10: Pré-aviso obrigatório calculado automaticamente 5 minutos antes do horário principal
    @Column(name = "horario_pre_aviso", nullable = false)
    private LocalTime horarioPreAviso;

    @Column(nullable = false)
    private Boolean ativo = true;

    // RNF03 / RN10: Guarda o ID do evento sincronizado no Google Agenda do usuário
    @Column(name = "google_event_id", length = 120)
    private String googleEventId;

    public Agendamento() {
    }

    public Agendamento(Long estoqueId, String nomeMedicamento, String dosagem, LocalTime horario, Boolean ativo) {
        this.estoqueId = estoqueId;
        this.nomeMedicamento = nomeMedicamento;
        this.dosagem = dosagem;
        this.horario = horario;
        this.ativo = ativo;
        calcularPreAviso();
    }

    // Executa automaticamente antes de salvar ou atualizar no MySQL (garante RN10 sempre)
    @PrePersist
    @PreUpdate
    public void calcularPreAviso() {
        if (this.horario != null) {
            this.horarioPreAviso = this.horario.minusMinutes(5);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEstoqueId() {
        return estoqueId;
    }

    public void setEstoqueId(Long estoqueId) {
        this.estoqueId = estoqueId;
    }

    public String getNomeMedicamento() {
        return nomeMedicamento;
    }

    public void setNomeMedicamento(String nomeMedicamento) {
        this.nomeMedicamento = nomeMedicamento;
    }

    public String getDosagem() {
        return dosagem;
    }

    public void setDosagem(String dosagem) {
        this.dosagem = dosagem;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
        calcularPreAviso();
    }

    public LocalTime getHorarioPreAviso() {
        return horarioPreAviso;
    }

    public void setHorarioPreAviso(LocalTime horarioPreAviso) {
        this.horarioPreAviso = horarioPreAviso;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getGoogleEventId() {
        return googleEventId;
    }

    public void setGoogleEventId(String googleEventId) {
        this.googleEventId = googleEventId;
    }
}