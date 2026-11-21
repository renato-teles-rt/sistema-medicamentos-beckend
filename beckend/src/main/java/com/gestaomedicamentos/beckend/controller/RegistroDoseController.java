package com.gestaomedicamentos.beckend.controller;

import com.gestaomedicamentos.beckend.model.RegistroDose;
import com.gestaomedicamentos.beckend.service.RegistroDoseService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/doses")
@CrossOrigin(origins = "*")
public class RegistroDoseController {

    private final RegistroDoseService registroDoseService;

    public RegistroDoseController(RegistroDoseService registroDoseService) {
        this.registroDoseService = registroDoseService;
    }

    // GET /api/doses/hoje -> Lista as doses previstas para hoje (ou gera automaticamente se ainda não existirem)
    @GetMapping("/hoje")
    public ResponseEntity<List<RegistroDose>> listarDosesDeHoje() {
        List<RegistroDose> doses = registroDoseService.listarOuGerarDosesDoDia(LocalDate.now());
        return ResponseEntity.ok(doses);
    }

    // PUT /api/doses/{id}/tomar -> Confirma que o paciente tomou a dose agora
    @PutMapping("/{id}/tomar")
    public ResponseEntity<RegistroDose> confirmarDoseTomada(
            @PathVariable Long id,
            @RequestParam(required = false) String observacao) {
        RegistroDose atualizada = registroDoseService.confirmarDoseTomada(id, observacao);
        return ResponseEntity.ok(atualizada);
    }

    // PUT /api/doses/{id}/pular -> Registra que a dose foi pulada (com observação opcional)
    @PutMapping("/{id}/pular")
    public ResponseEntity<RegistroDose> marcarDosePulada(
            @PathVariable Long id,
            @RequestParam(required = false) String observacao) {
        RegistroDose atualizada = registroDoseService.marcarDosePulada(id, observacao);
        return ResponseEntity.ok(atualizada);
    }

    // GET /api/doses/agendamento/{agendamentoId} -> Consulta o histórico completo de um medicamento
    @GetMapping("/agendamento/{agendamentoId}")
    public ResponseEntity<List<RegistroDose>> listarHistorico(@PathVariable Long agendamentoId) {
        List<RegistroDose> historico = registroDoseService.listarHistoricoPorAgendamento(agendamentoId);
        return ResponseEntity.ok(historico);
    }
}