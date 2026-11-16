package com.gestaomedicamentos.beckend.service;

import com.gestaomedicamentos.beckend.model.Usuario;
import com.gestaomedicamentos.beckend.model.VinculoFamiliar;
import com.gestaomedicamentos.beckend.repository.UsuarioRepository;
import com.gestaomedicamentos.beckend.repository.VinculoFamiliarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AuthVinculoService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VinculoFamiliarRepository vinculoFamiliarRepository;

    // 1. Login ou Cadastro Automático (Google SSO)
    public Usuario autenticarOuCadastrarGoogle(String nome, String email, String fotoUrl, String tipoPerfil) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(email);

        if (usuarioExistente.isPresent()) {
            // Se o usuário já existe, apenas retorna os dados dele
            return usuarioExistente.get();
        }

        // Se é o primeiro acesso, cria um novo usuário (o código de 6 dígitos é gerado sozinho)
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(nome);
        novoUsuario.setEmail(email);
        novoUsuario.setFotoUrl(fotoUrl);
        novoUsuario.setTipoPerfil(tipoPerfil != null ? tipoPerfil.toUpperCase() : "PACIENTE");

        return usuarioRepository.save(novoUsuario);
    }

    // 2. Recuperação de Perfil pelo E-mail
    public Usuario recuperarPerfilPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Nenhum perfil encontrado com este e-mail."));
    }

    // 3. Criar Vínculo Familiar (Cuidador digita o código de 6 letras do Paciente)
    public VinculoFamiliar criarVinculo(Long cuidadorId, String codigoVinculoPaciente) {
        Usuario cuidador = usuarioRepository.findById(cuidadorId)
                .orElseThrow(() -> new RuntimeException("Cuidador não encontrado."));

        Usuario paciente = usuarioRepository.findByCodigoVinculo(codigoVinculoPaciente.toUpperCase().trim())
                .orElseThrow(() -> new RuntimeException("Código de vínculo inválido ou paciente não encontrado."));

        if (cuidador.getId().equals(paciente.getId())) {
            throw new RuntimeException("Você não pode vincular o seu próprio perfil a si mesmo.");
        }

        // Verifica se o vínculo já existe para não duplicar
        Optional<VinculoFamiliar> vinculoExistente = vinculoFamiliarRepository
                .findByCuidadorIdAndPacienteId(cuidador.getId(), paciente.getId());

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

    // 4. Listar todos os Pacientes acompanhados por um Cuidador
    public List<VinculoFamiliar> listarPacientesDoCuidador(Long cuidadorId) {
        return vinculoFamiliarRepository.findByCuidadorIdAndAtivoTrue(cuidadorId);
    }

    // 5. Listar todos os Cuidadores de um Paciente
    public List<VinculoFamiliar> listarCuidadoresDoPaciente(Long pacienteId) {
        return vinculoFamiliarRepository.findByPacienteIdAndAtivoTrue(pacienteId);
    }
}