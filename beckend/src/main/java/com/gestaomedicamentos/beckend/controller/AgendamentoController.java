package com.gestaomedicamentos.beckend.controller;

import com.gestaomedicamentos.beckend.model.Agendamento;
import com.gestaomedicamentos.beckend.service.AgendamentoService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    // POST /api/agendamentos -> Cria um novo horário de medicação
    @PostMapping
    public ResponseEntity<Agendamento> criar(@RequestBody Agendamento agendamento) {
        Agendamento salvo = agendamentoService.criarAgendamento(agendamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    // PUT /api/agendamentos/{id} -> Atualiza nome, dosagem ou horário de um agendamento
    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> atualizar(@PathVariable Long id, @RequestBody Agendamento dadosAtualizados) {
        Agendamento atualizado = agendamentoService.atualizarAgendamento(id, dadosAtualizados);
        return ResponseEntity.ok(atualizado);
    }

    // DELETE /api/agendamentos/{id} -> Desativa o agendamento sem perder o histórico
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        agendamentoService.desativarAgendamento(id);
        return ResponseEntity.noContent().build();
    }
}