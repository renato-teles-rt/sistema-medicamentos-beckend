package com.gestaomedicamentos.beckend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "agendamento")
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Guarda o ID do item na tabela Estoque de Jason sem travar sua compilação agora
    @Column(name = "estoque_id", nullable = false)
    private Long estoqueId;

    @Column(name = "nome_medicamento", nullable = false)
    private String nomeMedicamento;

    @Column(nullable = false)
    private String dosagem;

    // Guarda apenas a hora e minuto (ex: 08:00) para ordenar as doses do dia
    @Column(nullable = false)
    private LocalTime horario;

    @Column(nullable = false)
    private Boolean ativo = true;

    // Construtor vazio obrigatório para o Hibernate (JPA)
    public Agendamento() {
    }

    // Getters e Setters
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
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}