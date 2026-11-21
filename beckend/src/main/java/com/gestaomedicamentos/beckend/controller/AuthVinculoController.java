package com.gestaomedicamentos.beckend.controller;

import com.gestaomedicamentos.beckend.model.Usuario;
import com.gestaomedicamentos.beckend.model.VinculoFamiliar;
import com.gestaomedicamentos.beckend.service.AuthVinculoService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthVinculoController {

    @Autowired private AuthVinculoService authVinculoService;

    // 1. Rota de Login / Cadastro com Google: POST http://localhost:8080/api/auth/google
    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(@RequestBody Map<String, String> dados) {
        try {
            String nome = dados.get("nome");
            String email = dados.get("email");
            String fotoUrl = dados.get("fotoUrl");
            String tipoPerfil = dados.get("tipoPerfil");

            if (nome == null || email == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Nome e e-mail são obrigatórios."));
            }

            Usuario usuario =
                    authVinculoService.autenticarOuCadastrarGoogle(
                            nome, email, fotoUrl, tipoPerfil);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // 2. Rota de Recuperação de Perfil: GET http://localhost:8080/api/auth/recuperar?email=...
    @GetMapping("/recuperar")
    public ResponseEntity<?> recuperarPerfil(@RequestParam String email) {
        try {
            Usuario usuario = authVinculoService.recuperarPerfilPorEmail(email);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("erro", e.getMessage()));
        }
    }

    // 3. Rota para Criar Vínculo Familiar: POST http://localhost:8080/api/auth/vincular
    @PostMapping("/vincular")
    public ResponseEntity<?> vincularFamiliar(@RequestBody Map<String, Object> dados) {
        try {
            Long cuidadorId = Long.valueOf(dados.get("cuidadorId").toString());
            String codigoVinculo = dados.get("codigoVinculo").toString();

            VinculoFamiliar vinculo = authVinculoService.criarVinculo(cuidadorId, codigoVinculo);
            return ResponseEntity.ok(vinculo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // 4. Listar Pacientes de um Cuidador: GET
    // http://localhost:8080/api/auth/cuidador/{cuidadorId}/pacientes
    @GetMapping("/cuidador/{cuidadorId}/pacientes")
    public ResponseEntity<List<VinculoFamiliar>> listarPacientes(@PathVariable Long cuidadorId) {
        return ResponseEntity.ok(authVinculoService.listarPacientesDoCuidador(cuidadorId));
    }

    // 5. Listar Cuidadores de um Paciente: GET
    // http://localhost:8080/api/auth/paciente/{pacienteId}/cuidadores
    @GetMapping("/paciente/{pacienteId}/cuidadores")
    public ResponseEntity<List<VinculoFamiliar>> listarCuidadores(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(authVinculoService.listarCuidadoresDoPaciente(pacienteId));
    }

    @PostMapping("/google-token")
    public ResponseEntity<?> loginComGoogleToken(@RequestBody Map<String, String> dados) {
        try {
            Usuario usuario =
                    authVinculoService.autenticarComGoogleToken(
                            dados.get("idToken"), dados.get("tipoPerfil"));
            return ResponseEntity.ok(usuario);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    // 6. Desvincular Cuidador/Paciente: DELETE http://localhost:8080/api/auth/vincular/{vinculoId}
    @DeleteMapping("/vincular/{vinculoId}")
    public ResponseEntity<?> desvincularFamiliar(@PathVariable Long vinculoId) {
        try {
            authVinculoService.desvincularFamiliar(vinculoId);
            return ResponseEntity.ok(Map.of("mensagem", "Vínculo encerrado com sucesso."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }
}
