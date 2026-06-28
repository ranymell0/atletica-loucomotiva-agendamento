package br.ufop.agendamento.controller;

import br.ufop.agendamento.model.Agendamento;
import br.ufop.agendamento.repository.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/agendamentos")
public class AgendamentoController {

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @GetMapping
    public List<Agendamento> listar() {
        return agendamentoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agendamento> buscarPorId(@PathVariable UUID id) {
        return agendamentoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Agendamento criar(@RequestBody Agendamento agendamento) {
        return agendamentoRepository.save(agendamento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agendamento> atualizar(@PathVariable UUID id, @RequestBody Agendamento agendamentoAtualizado) {
        return agendamentoRepository.findById(id).map(agendamento -> {
            agendamento.setUsuario(agendamentoAtualizado.getUsuario());
            agendamento.setEspaco(agendamentoAtualizado.getEspaco());
            agendamento.setEsporte(agendamentoAtualizado.getEsporte());
            agendamento.setHorario(agendamentoAtualizado.getHorario());
            agendamento.setData(agendamentoAtualizado.getData());
            agendamento.setHoraInicio(agendamentoAtualizado.getHoraInicio());
            agendamento.setHoraFim(agendamentoAtualizado.getHoraFim());
            agendamento.setTipo(agendamentoAtualizado.getTipo());
            agendamento.setStatus(agendamentoAtualizado.getStatus());
            return ResponseEntity.ok(agendamentoRepository.save(agendamento));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        return agendamentoRepository.findById(id).map(agendamento -> {
            agendamentoRepository.delete(agendamento);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}