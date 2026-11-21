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
import org.springframework.stereotype.Service;

@Service
public class AuthVinculoService {

    @Autowired private UsuarioRepository usuarioRepository;

    @Autowired private VinculoFamiliarRepository vinculoFamiliarRepository;

    @Value("${google.client.id}")
    private String googleClientId;

    // 1. Autenticação Real com Token Oficial do Google (Mobile Flutter / React OAuth)
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

            // Fallback seguro caso a conta Google tenha apenas um nome cadastrado sem sobrenome
            String nomeFinal =
                    (nomeGoogle != null && !nomeGoogle.trim().isEmpty())
                            ? nomeGoogle.trim().replaceAll("\\s+", " ")
                            : "Usuário Google";

            return usuarioRepository
                    .findByEmail(emailLimpo)
                    .orElseGet(
                            () -> {
                                Usuario novoUsuario = new Usuario();
                                novoUsuario.setNome(nomeFinal);
                                novoUsuario.setEmail(emailLimpo);
                                novoUsuario.setFotoUrl(fotoUrl);
                                novoUsuario.setTipoPerfil(
                                        tipoPerfil != null ? tipoPerfil.toUpperCase() : "PACIENTE");
                                return usuarioRepository.save(novoUsuario);
                            });
        } catch (Exception e) {
            throw new RuntimeException("Falha na autenticação com o Google: " + e.getMessage());
        }
    }

    // 2. Login ou Cadastro Automático (Modo Formulário/Fallback) exigindo Nome + Sobrenome e E-mail
    // válido
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

        // Normaliza múltiplos espaços internos para um único espaço
        String nomeLimpo = nome.trim().replaceAll("\\s+", " ");
        String emailLimpo = email.trim().toLowerCase();

        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(emailLimpo);

        if (usuarioExistente.isPresent()) {
            return usuarioExistente.get();
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(nomeLimpo);
        novoUsuario.setEmail(emailLimpo);
        novoUsuario.setFotoUrl(fotoUrl);
        novoUsuario.setTipoPerfil(tipoPerfil != null ? tipoPerfil.toUpperCase() : "PACIENTE");

        return usuarioRepository.save(novoUsuario);
    }

    // 3. Recuperação de Perfil pelo E-mail
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

    // 4. Criar Vínculo Familiar (Cuidador digita o código de 6 letras do Paciente)
    public VinculoFamiliar criarVinculo(Long cuidadorId, String codigoVinculoPaciente) {
        Usuario cuidador =
                usuarioRepository
                        .findById(cuidadorId)
                        .orElseThrow(() -> new RuntimeException("Cuidador não encontrado."));

        Usuario paciente =
                usuarioRepository
                        .findByCodigoVinculo(codigoVinculoPaciente.toUpperCase().trim())
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Código de vínculo inválido ou paciente não encontrado."));

        if (cuidador.getId().equals(paciente.getId())) {
            throw new RuntimeException("Você não pode vincular o seu próprio perfil a si mesmo.");
        }

        Optional<VinculoFamiliar> vinculoExistente =
                vinculoFamiliarRepository.findByCuidadorIdAndPacienteId(
                        cuidador.getId(), paciente.getId());

        if (vinculoExistente.isPresent()) {
            VinculoFamiliar vinculo = vinculoExistente.get();
            vinculo.setAtivo(true);
            return vinculoFamiliarRepository.save(vinculo);
        }

        VinculoFamiliar novoVinculo = new VinculoFamiliar();
        novoVinculo.setCuidador(cuidador);
        novoVinculo.setPaciente(paciente);
        novoVinculo.setAtivo(true);

        return vinculoFamiliarRepository.save(novoVinculo);
    }

    // 5. Listar todos os Pacientes acompanhados por um Cuidador
    public List<VinculoFamiliar> listarPacientesDoCuidador(Long cuidadorId) {
        return vinculoFamiliarRepository.findByCuidadorIdAndAtivoTrue(cuidadorId);
    }

    // 6. Listar todos os Cuidadores de um Paciente
    public List<VinculoFamiliar> listarCuidadoresDoPaciente(Long pacienteId) {
        return vinculoFamiliarRepository.findByPacienteIdAndAtivoTrue(pacienteId);
    }
}
