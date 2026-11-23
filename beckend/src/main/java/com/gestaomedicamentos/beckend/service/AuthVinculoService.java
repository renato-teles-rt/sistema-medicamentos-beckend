package com.gestaomedicamentos.beckend.service;

import com.gestaomedicamentos.beckend.model.Usuario;
import com.gestaomedicamentos.beckend.model.VinculoFamiliar;
import com.gestaomedicamentos.beckend.repository.UsuarioRepository;
import com.gestaomedicamentos.beckend.repository.VinculoFamiliarRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthVinculoService {

    private static final int LIMITE_MAXIMO_CUIDADORES = 2;

    @Autowired private UsuarioRepository usuarioRepository;

    @Autowired private VinculoFamiliarRepository vinculoFamiliarRepository;

    @Value("${google.client.id}")
    private String googleClientId;

    @Transactional
    public Usuario autenticarComGoogleToken(String idTokenString, String tipoPerfil) {
        if (idTokenString == null || idTokenString.trim().isEmpty()) {
            throw new RuntimeException("O token de autenticação do Google é obrigatório.");
        }

        try {
            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                            .setAudience(Collections.singletonList(googleClientId))
                            .build();

            GoogleIdToken idToken = verifier.verify(idTokenString.trim());
            if (idToken == null) {
                throw new RuntimeException("Token do Google inválido ou expirado.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String emailLimpo = payload.getEmail().toLowerCase().trim();
            String nomeGoogle = (String) payload.get("name");
            String fotoUrl = (String) payload.get("picture");

            String nomeFinal =
                    (nomeGoogle != null && !nomeGoogle.trim().isEmpty())
                            ? nomeGoogle.trim().replaceAll("\\s+", " ")
                            : "Usuário Google";

            String perfilDesejado =
                    (tipoPerfil != null && !tipoPerfil.trim().isEmpty())
                            ? tipoPerfil.trim().toUpperCase()
                            : "PACIENTE";

            Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(emailLimpo);
            if (usuarioExistente.isPresent()) {
                Usuario existente = usuarioExistente.get();
                if (tipoPerfil != null
                        && !tipoPerfil.trim().isEmpty()
                        && !existente.getTipoPerfil().equalsIgnoreCase(perfilDesejado)) {
                    throw new RuntimeException(
                            "Este e-mail já está cadastrado como "
                                    + existente.getTipoPerfil()
                                    + ". Selecione o perfil correto para entrar.");
                }
                return existente;
            }

            Usuario novoUsuario = new Usuario();
            novoUsuario.setNome(nomeFinal);
            novoUsuario.setEmail(emailLimpo);
            novoUsuario.setFotoUrl(fotoUrl);
            novoUsuario.setTipoPerfil(perfilDesejado);
            return usuarioRepository.save(novoUsuario);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Falha na autenticação com o Google: " + e.getMessage());
        }
    }

    @Transactional
    public Usuario autenticarOuCadastrarGoogle(
            String nome, String email, String fotoUrl, String tipoPerfil) {
        if (nome == null
                || !nome.trim().matches("^[A-Za-zÀ-ÖØ-öø-ÿ]{2,}(?:\\s+[A-Za-zÀ-ÖØ-öø-ÿ]{2,})+$")) {
            throw new RuntimeException(
                    "Informe seu Nome e Sobrenome completos utilizando apenas letras.");
        }

        if (email == null
                || !email.trim()
                        .toLowerCase()
                        .matches("^(?=.*[a-z])[a-z0-9._-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
            throw new RuntimeException(
                    "Informe um e-mail válido contendo letras (ex: nome@gmail.com).");
        }

        String nomeLimpo = nome.trim().replaceAll("\\s+", " ");
        String emailLimpo = email.trim().toLowerCase();
        String perfilDesejado =
                (tipoPerfil != null && !tipoPerfil.trim().isEmpty())
                        ? tipoPerfil.trim().toUpperCase()
                        : "PACIENTE";

        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(emailLimpo);

        if (usuarioExistente.isPresent()) {
            Usuario existente = usuarioExistente.get();
            if (tipoPerfil != null
                    && !tipoPerfil.trim().isEmpty()
                    && !existente.getTipoPerfil().equalsIgnoreCase(perfilDesejado)) {
                throw new RuntimeException(
                        "Este e-mail já está cadastrado como "
                                + existente.getTipoPerfil()
                                + ". Selecione o perfil correto para entrar.");
            }
            return existente;
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(nomeLimpo);
        novoUsuario.setEmail(emailLimpo);
        novoUsuario.setFotoUrl(fotoUrl);
        novoUsuario.setTipoPerfil(perfilDesejado);

        return usuarioRepository.save(novoUsuario);
    }

    public Usuario recuperarPerfilPorEmail(String email) {
        if (email == null
                || !email.trim()
                        .toLowerCase()
                        .matches("^(?=.*[a-z])[a-z0-9._-]+@[a-z0-9.-]+\\.[a-z]{2,}$")) {
            throw new RuntimeException("Informe um e-mail válido contendo letras.");
        }

        return usuarioRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(
                        () -> new RuntimeException("Nenhum perfil encontrado com este e-mail."));
    }

    @Transactional
    public VinculoFamiliar criarVinculo(Long cuidadorId, String codigoVinculoPaciente) {
        if (codigoVinculoPaciente == null || codigoVinculoPaciente.trim().length() != 6) {
            throw new RuntimeException("Informe o código de vínculo de 6 dígitos do paciente.");
        }

        Usuario cuidador =
                usuarioRepository
                        .findById(cuidadorId)
                        .orElseThrow(() -> new RuntimeException("Cuidador não encontrado."));

        if (!"CUIDADOR".equalsIgnoreCase(cuidador.getTipoPerfil())) {
            throw new RuntimeException(
                    "Apenas contas com perfil de CUIDADOR podem inserir um código de vínculo.");
        }

        Usuario paciente =
                usuarioRepository
                        .findByCodigoVinculo(codigoVinculoPaciente.toUpperCase().trim())
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Código de vínculo inválido ou paciente não encontrado."));

        if (!"PACIENTE".equalsIgnoreCase(paciente.getTipoPerfil())) {
            throw new RuntimeException("O código informado não pertence a uma conta de PACIENTE.");
        }

        if (cuidador.getId().equals(paciente.getId())) {
            throw new RuntimeException("Você não pode vincular o seu próprio perfil a si mesmo.");
        }

        Optional<VinculoFamiliar> vinculoExistente =
                vinculoFamiliarRepository.findByCuidadorIdAndPacienteId(
                        cuidador.getId(), paciente.getId());

        if (vinculoExistente.isPresent()) {
            VinculoFamiliar vinculo = vinculoExistente.get();
            if (Boolean.TRUE.equals(vinculo.getAtivo())) {
                throw new RuntimeException("Você já está vinculado(a) a este paciente.");
            }

            long totalCuidadoresAtivos =
                    vinculoFamiliarRepository.countByPacienteIdAndAtivoTrue(paciente.getId());
            if (totalCuidadoresAtivos >= LIMITE_MAXIMO_CUIDADORES) {
                throw new RuntimeException(
                        "Este paciente já atingiu o limite máximo de "
                                + LIMITE_MAXIMO_CUIDADORES
                                + " cuidadores ativos.");
            }

            vinculo.setAtivo(true);
            return vinculoFamiliarRepository.save(vinculo);
        }

        long totalCuidadoresAtivos =
                vinculoFamiliarRepository.countByPacienteIdAndAtivoTrue(paciente.getId());
        if (totalCuidadoresAtivos >= LIMITE_MAXIMO_CUIDADORES) {
            throw new RuntimeException(
                    "Este paciente já atingiu o limite máximo de "
                            + LIMITE_MAXIMO_CUIDADORES
                            + " cuidadores ativos.");
        }

        VinculoFamiliar novoVinculo = new VinculoFamiliar();
        novoVinculo.setCuidador(cuidador);
        novoVinculo.setPaciente(paciente);
        novoVinculo.setAtivo(true);

        return vinculoFamiliarRepository.save(novoVinculo);
    }

    public void validarAcessoCuidador(Long cuidadorId, Long solicitanteId) {
        if (solicitanteId == null) {
            return;
        }
        if (!cuidadorId.equals(solicitanteId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso negado: você só pode consultar os pacientes do seu próprio perfil.");
        }
    }

    public void validarAcessoPaciente(Long pacienteId, Long solicitanteId) {
        if (solicitanteId == null) {
            return;
        }
        if (pacienteId.equals(solicitanteId)) {
            return;
        }
        boolean possuiVinculoAtivo =
                vinculoFamiliarRepository.existsByCuidadorIdAndPacienteIdAndAtivoTrue(
                        solicitanteId, pacienteId);
        if (!possuiVinculoAtivo) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Acesso negado: sem vínculo familiar ativo com este paciente.");
        }
    }

    @Transactional
    public void desvincularFamiliar(Long vinculoId, Long solicitanteId) {
        VinculoFamiliar vinculo =
                vinculoFamiliarRepository
                        .findById(vinculoId)
                        .orElseThrow(
                                () -> new RuntimeException("Vínculo familiar não encontrado."));

        if (solicitanteId != null) {
            Long cuidadorId =
                    vinculo.getCuidador() != null ? vinculo.getCuidador().getId() : null;
            Long pacienteId =
                    vinculo.getPaciente() != null ? vinculo.getPaciente().getId() : null;

            boolean autorizado =
                    solicitanteId.equals(cuidadorId) || solicitanteId.equals(pacienteId);

            if (!autorizado) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Acesso negado: você não tem permissão para encerrar este vínculo.");
            }
        }

        vinculo.setAtivo(false);
        vinculoFamiliarRepository.save(vinculo);
    }

    @Transactional
    public void desvincularFamiliar(Long vinculoId) {
        desvincularFamiliar(vinculoId, null);
    }

    public List<VinculoFamiliar> listarPacientesDoCuidador(Long cuidadorId) {
        return vinculoFamiliarRepository.findByCuidadorIdAndAtivoTrue(cuidadorId);
    }

    public List<VinculoFamiliar> listarCuidadoresDoPaciente(Long pacienteId) {
        return vinculoFamiliarRepository.findByPacienteIdAndAtivoTrue(pacienteId);
    }
}