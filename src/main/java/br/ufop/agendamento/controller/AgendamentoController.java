package br.ufop.agendamento.controller;

import br.ufop.agendamento.model.Agendamento;
import br.ufop.agendamento.service.AgendamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Controller responsável pelas rotas de agendamentos
@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    // Injeção do serviço de agendamentos
    @Autowired
    private AgendamentoService agendamentoService;

    // Retorna todos os agendamentos
    @GetMapping
    public List<Agendamento> listar() {
        return agendamentoService.listarTodos();
    }

    // Retorna um agendamento pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> buscarPorId(@PathVariable UUID id) {
        return agendamentoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Cria um novo agendamento
    @PostMapping
    public Agendamento criar(@RequestBody Agendamento agendamento) {
        return agendamentoService.salvar(agendamento);
    }

    // Atualiza um agendamento existente
    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> atualizar(@PathVariable UUID id, @RequestBody Agendamento agendamentoAtualizado) {
        return agendamentoService.atualizar(id, agendamentoAtualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Confirma um agendamento pendente
    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<Agendamento> confirmar(@PathVariable UUID id) {
        return agendamentoService.confirmar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Cancela um agendamento
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Agendamento> cancelar(@PathVariable UUID id) {
        return agendamentoService.cancelar(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Remove um agendamento pelo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        return agendamentoService.deletar(id)
                ? ResponseEntity.ok().<Void>build()
                : ResponseEntity.notFound().build();
    }
}