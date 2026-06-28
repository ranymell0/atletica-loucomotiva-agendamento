package br.ufop.agendamento.controller;

import br.ufop.agendamento.model.Esporte;
import br.ufop.agendamento.repository.EsporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/esportes")
public class EsporteController {

    @Autowired
    private EsporteRepository esporteRepository;

    @GetMapping
    public List<Esporte> listar() {
        return esporteRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Esporte> buscarPorId(@PathVariable UUID id) {
        return esporteRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Esporte criar(@RequestBody Esporte esporte) {
        return esporteRepository.save(esporte);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Esporte> atualizar(@PathVariable UUID id, @RequestBody Esporte esporteAtualizado) {
        return esporteRepository.findById(id).map(esporte -> {
            esporte.setNome(esporteAtualizado.getNome());
            esporte.setDescricao(esporteAtualizado.getDescricao());
            return ResponseEntity.ok(esporteRepository.save(esporte));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        return esporteRepository.findById(id).map(esporte -> {
            esporteRepository.delete(esporte);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}