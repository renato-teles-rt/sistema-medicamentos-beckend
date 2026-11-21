package com.gestaomedicamentos.beckend.controller;

import com.gestaomedicamentos.beckend.model.Usuario;
import com.gestaomedicamentos.beckend.model.VinculoFamiliar;
import com.gestaomedicamentos.beckend.service.AuthVinculoService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

            if (nome == null || nome.isBlank() || email == null || email.isBlank()) {
                return erroJson(HttpStatus.BAD_REQUEST, "Nome e e-mail são obrigatórios.");
            }

            Usuario usuario =
                    authVinculoService.autenticarOuCadastrarGoogle(
                            nome, email, fotoUrl, tipoPerfil);
            return ResponseEntity.ok(usuario);
        } catch (ResponseStatusException e) {
            return responderStatus(e);
        } catch (Exception e) {
            return erroJson(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 2. Rota de Recuperação de Perfil: GET http://localhost:8080/api/auth/recuperar?email=...
    @GetMapping("/recuperar")
    public ResponseEntity<?> recuperarPerfil(@RequestParam String email) {
        try {
            Usuario usuario = authVinculoService.recuperarPerfilPorEmail(email);
            return ResponseEntity.ok(usuario);
        } catch (ResponseStatusException e) {
            return responderStatus(e);
        } catch (Exception e) {
            return erroJson(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    // 3. Rota para Criar Vínculo Familiar: POST http://localhost:8080/api/auth/vincular
    @PostMapping("/vincular")
    public ResponseEntity<?> vincularFamiliar(@RequestBody Map<String, Object> dados) {
        try {
            if (dados.get("cuidadorId") == null || dados.get("codigoVinculo") == null) {
                return erroJson(
                        HttpStatus.BAD_REQUEST, "cuidadorId e codigoVinculo são obrigatórios.");
            }

            Long cuidadorId = Long.valueOf(dados.get("cuidadorId").toString());
            String codigoVinculo = dados.get("codigoVinculo").toString().trim();

            VinculoFamiliar vinculo = authVinculoService.criarVinculo(cuidadorId, codigoVinculo);
            return ResponseEntity.ok(vinculo);
        } catch (ResponseStatusException e) {
            return responderStatus(e);
        } catch (NumberFormatException e) {
            return erroJson(HttpStatus.BAD_REQUEST, "cuidadorId inválido.");
        } catch (Exception e) {
            return erroJson(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 4. Listar Pacientes de um Cuidador: GET
    // http://localhost:8080/api/auth/cuidador/{cuidadorId}/pacientes
    @GetMapping("/cuidador/{cuidadorId}/pacientes")
    public ResponseEntity<?> listarPacientes(
            @PathVariable Long cuidadorId,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long solicitanteId) {
        try {
            authVinculoService.validarAcessoCuidador(cuidadorId, solicitanteId);
            List<VinculoFamiliar> pacientes =
                    authVinculoService.listarPacientesDoCuidador(cuidadorId);
            return ResponseEntity.ok(pacientes);
        } catch (ResponseStatusException e) {
            return responderStatus(e);
        } catch (Exception e) {
            return erroJson(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 5. Listar Cuidadores de um Paciente: GET
    // http://localhost:8080/api/auth/paciente/{pacienteId}/cuidadores
    @GetMapping("/paciente/{pacienteId}/cuidadores")
    public ResponseEntity<?> listarCuidadores(
            @PathVariable Long pacienteId,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long solicitanteId) {
        try {
            authVinculoService.validarAcessoPaciente(pacienteId, solicitanteId);
            List<VinculoFamiliar> cuidadores =
                    authVinculoService.listarCuidadoresDoPaciente(pacienteId);
            return ResponseEntity.ok(cuidadores);
        } catch (ResponseStatusException e) {
            return responderStatus(e);
        } catch (Exception e) {
            return erroJson(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/google-token")
    public ResponseEntity<?> loginComGoogleToken(@RequestBody Map<String, String> dados) {
        try {
            Usuario usuario =
                    authVinculoService.autenticarComGoogleToken(
                            dados.get("idToken"), dados.get("tipoPerfil"));
            return ResponseEntity.ok(usuario);
        } catch (ResponseStatusException e) {
            return responderStatus(e);
        } catch (RuntimeException e) {
            return erroJson(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // 6. Desvincular Cuidador/Paciente: DELETE http://localhost:8080/api/auth/vincular/{vinculoId}
    @DeleteMapping("/vincular/{vinculoId}")
    public ResponseEntity<?> desvincularFamiliar(
            @PathVariable Long vinculoId,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long solicitanteId) {
        try {
            authVinculoService.desvincularFamiliar(vinculoId, solicitanteId);
            Map<String, String> corpo = new HashMap<>();
            corpo.put("mensagem", "Vínculo encerrado com sucesso.");
            return ResponseEntity.ok(corpo);
        } catch (ResponseStatusException e) {
            return responderStatus(e);
        } catch (Exception e) {
            return erroJson(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private ResponseEntity<?> responderStatus(ResponseStatusException ex) {
        String mensagem = ex.getReason() != null ? ex.getReason() : "Operação não permitida.";
        return ResponseEntity.status(ex.getStatusCode()).body(criarMapaErro(mensagem));
    }

    private ResponseEntity<?> erroJson(HttpStatus status, String mensagem) {
        String msgSegura =
                (mensagem != null && !mensagem.isBlank()) ? mensagem : "Erro na requisição.";
        return ResponseEntity.status(status).body(criarMapaErro(msgSegura));
    }

    private Map<String, String> criarMapaErro(String mensagem) {
        Map<String, String> mapa = new HashMap<>();
        mapa.put("erro", mensagem);
        return mapa;
    }
}