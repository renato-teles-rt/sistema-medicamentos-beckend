package com.gestaomedicamentos.beckend.controller;

import com.gestaomedicamentos.beckend.model.Agendamento;
import com.gestaomedicamentos.beckend.service.AgendamentoService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agendamentos")
@CrossOrigin(origins = "*")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    // GET /api/agendamentos -> Lista todos os agendamentos ativos ordenados por horário
    @GetMapping
    public ResponseEntity<List<Agendamento>> listarAtivos() {
        List<Agendamento> lista = agendamentoService.listarAtivosOrdenados();
        return ResponseEntity.ok(lista);
    }

    // GET /api/agendamentos/{id} -> Busca um agendamento específico pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> buscarPorId(@PathVariable Long id) {
        Agendamento agendamento = agendamentoService.buscarPorId(id);
        return ResponseEntity.ok(agendamento);
    }

    // POST /api/agendamentos -> Cria horário via App/Web validando conflito (RN03/RN09) e sincronizando Google Agenda (RNF03/RN10)
    @PostMapping
    public ResponseEntity<?> criar(
            @RequestBody Agendamento agendamento,
            @RequestHeader(value = "X-Google-Token", required = false) String googleToken) {
        try {
            Agendamento salvo = agendamentoService.criarAgendamento(agendamento, googleToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (IllegalStateException conflito) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", conflito.getMessage()));
        } catch (IllegalArgumentException invalido) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", invalido.getMessage()));
        }
    }

    // PUT /api/agendamentos/{id} -> Atualiza horário via App/Web validando conflito (RN03/RN09) e sincronizando Google Agenda (RNF03/RN10)
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @RequestBody Agendamento dadosAtualizados,
            @RequestHeader(value = "X-Google-Token", required = false) String googleToken) {
        try {
            Agendamento atualizado = agendamentoService.atualizarAgendamento(id, dadosAtualizados, googleToken);
            return ResponseEntity.ok(atualizado);
        } catch (IllegalStateException conflito) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", conflito.getMessage()));
        } catch (IllegalArgumentException invalido) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", invalido.getMessage()));
        }
    }

    // DELETE /api/agendamentos/{id} -> Desativa o agendamento sem perder o histórico
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        agendamentoService.desativarAgendamento(id);
        return ResponseEntity.noContent().build();
    }
}