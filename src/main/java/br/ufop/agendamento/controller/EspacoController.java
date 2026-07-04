package br.ufop.agendamento.controller;

import br.ufop.agendamento.model.Espaco;
import br.ufop.agendamento.service.EspacoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// Controller responsável pelas rotas de espaços
@RestController
@RequestMapping("/espacos")
public class EspacoController {

    // Injeção do serviço de espaços
    @Autowired
    private EspacoService espacoService;

    // Retorna todos os espaços
    @GetMapping
    public List<Espaco> listar() {
        return espacoService.listarTodos();
    }

    // Retorna um espaço pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<Espaco> buscarPorId(@PathVariable UUID id) {
        return espacoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Cria um novo espaço
    @PostMapping
    public Espaco criar(@RequestBody Espaco espaco) {
        return espacoService.salvar(espaco);
    }

    // Atualiza um espaço existente
    @PutMapping("/{id}")
    public ResponseEntity<Espaco> atualizar(@PathVariable UUID id, @RequestBody Espaco espacoAtualizado) {
        return espacoService.atualizar(id, espacoAtualizado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Remove um espaço pelo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        return espacoService.deletar(id)
                ? ResponseEntity.ok().<Void>build()
                : ResponseEntity.notFound().build();
    }
}