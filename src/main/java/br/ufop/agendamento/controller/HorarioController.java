package br.ufop.agendamento.controller;

import br.ufop.agendamento.model.Horario;
import br.ufop.agendamento.repository.HorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/horarios")
public class HorarioController {

    @Autowired
    private HorarioRepository horarioRepository;

    @GetMapping
    public List<Horario> listar() {
        return horarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Horario> buscarPorId(@PathVariable UUID id) {
        return horarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Horario criar(@RequestBody Horario horario) {
        return horarioRepository.save(horario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Horario> atualizar(@PathVariable UUID id, @RequestBody Horario horarioAtualizado) {
        return horarioRepository.findById(id).map(horario -> {
            horario.setEspaco(horarioAtualizado.getEspaco());
            horario.setHoraInicio(horarioAtualizado.getHoraInicio());
            horario.setHoraFim(horarioAtualizado.getHoraFim());
            horario.setDiaSemana(horarioAtualizado.getDiaSemana());
            horario.setAtivo(horarioAtualizado.getAtivo());
            return ResponseEntity.ok(horarioRepository.save(horario));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        return horarioRepository.findById(id).map(horario -> {
            horarioRepository.delete(horario);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}