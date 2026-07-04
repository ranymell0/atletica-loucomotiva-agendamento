package br.ufop.agendamento.controller;

import br.ufop.agendamento.model.Horario;
import br.ufop.agendamento.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Controller responsável pelas rotas de horários
@RestController
@RequestMapping("/horarios")
public class HorarioController {

    // Injeção do serviço de horários
    @Autowired
    private HorarioService horarioService;

    // Retorna todos os horários
    @GetMapping
    public List<Horario> listar() {
        return horarioService.listarTodos();
    }

    // Retorna um horário pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<Horario> buscarPorId(@PathVariable UUID id) {
        return horarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Cria um novo horário
    @PostMapping
    public Horario criar(@RequestBody Horario horario) {
        return horarioService.salvar(horario);
    }

    // Atualiza um horário existente
    @PutMapping("/{id}")
    public ResponseEntity<Horario> atualizar(@PathVariable UUID id, @RequestBody Horario horarioAtualizado) {
        return horarioService.atualizar(id, horarioAtualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Remove um horário pelo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        return horarioService.deletar(id)
                ? ResponseEntity.ok().<Void>build()
                : ResponseEntity.notFound().build();
    }
}