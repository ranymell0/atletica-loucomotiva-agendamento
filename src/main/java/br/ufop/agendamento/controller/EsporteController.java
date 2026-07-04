package br.ufop.agendamento.controller;

import br.ufop.agendamento.model.Esporte;
import br.ufop.agendamento.service.EsporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Controller responsável pelas rotas de esportes
@RestController
@RequestMapping("/esportes")
public class EsporteController {

    // Injeção do serviço de esportes
    @Autowired
    private EsporteService esporteService;

    // Retorna todos os esportes
    @GetMapping
    public List<Esporte> listar() {
        return esporteService.listarTodos();
    }

    // Retorna um esporte pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<Esporte> buscarPorId(@PathVariable UUID id) {
        return esporteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Cria um novo esporte
    @PostMapping
    public Esporte criar(@RequestBody Esporte esporte) {
        return esporteService.salvar(esporte);
    }

    // Atualiza um esporte existente
    @PutMapping("/{id}")
    public ResponseEntity<Esporte> atualizar(@PathVariable UUID id, @RequestBody Esporte esporteAtualizado) {
        return esporteService.atualizar(id, esporteAtualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Remove um esporte pelo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        return esporteService.deletar(id)
                ? ResponseEntity.ok().<Void>build()
                : ResponseEntity.notFound().build();
    }
}