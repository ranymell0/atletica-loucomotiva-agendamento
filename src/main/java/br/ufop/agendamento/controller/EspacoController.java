package br.ufop.agendamento.controller;

import br.ufop.agendamento.model.Espaco;
import br.ufop.agendamento.repository.EspacoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/espacos")
public class EspacoController {

    @Autowired
    private EspacoRepository espacoRepository;

    @GetMapping
    public List<Espaco> listar() {
        return espacoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Espaco> buscarPorId(@PathVariable UUID id) {
        return espacoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Espaco criar(@RequestBody Espaco espaco) {
        return espacoRepository.save(espaco);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Espaco> atualizar(@PathVariable UUID id, @RequestBody Espaco espacoAtualizado) {
        return espacoRepository.findById(id).map(espaco -> {
            espaco.setNome(espacoAtualizado.getNome());
            espaco.setDescricao(espacoAtualizado.getDescricao());
            espaco.setCapacidadeMaxima(espacoAtualizado.getCapacidadeMaxima());
            espaco.setAtivo(espacoAtualizado.getAtivo());
            return ResponseEntity.ok(espacoRepository.save(espaco));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        return espacoRepository.findById(id).map(espaco -> {
            espacoRepository.delete(espaco);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}