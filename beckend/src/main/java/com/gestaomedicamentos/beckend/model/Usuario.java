package com.gestaomedicamentos.beckend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório")
    @Column(nullable = false)
    private String nome;

    @Email(message = "E-mail inválido")
    @NotBlank(message = "O e-mail é obrigatório")
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "tipo_perfil", nullable = false)
    private String tipoPerfil; // Pode ser "PACIENTE" ou "CUIDADOR"

    @Column(name = "codigo_vinculo", unique = true, length = 6)
    private String codigoVinculo;

    // Gera automaticamente um código de 6 letras/números antes de salvar um novo usuário
    @PrePersist
    public void gerarCodigoVinculo() {
        if (this.codigoVinculo == null) {
            this.codigoVinculo = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        }
    }

    // Construtor vazio obrigatório para o Hibernate/JPA
    public Usuario() {}

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getTipoPerfil() {
        return tipoPerfil;
    }

    public void setTipoPerfil(String tipoPerfil) {
        this.tipoPerfil = tipoPerfil;
    }

    public String getCodigoVinculo() {
        return codigoVinculo;
    }

    public void setCodigoVinculo(String codigoVinculo) {
        this.codigoVinculo = codigoVinculo;
    }
}
