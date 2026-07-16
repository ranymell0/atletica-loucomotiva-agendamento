package br.ufop.agendamento.controller;

import br.ufop.agendamento.exception.EmailDuplicadoException;
import br.ufop.agendamento.model.Usuario;
import br.ufop.agendamento.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// Controller responsável pelas rotas de usuários
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    // Injeção do serviço de usuários
    @Autowired
    private UsuarioService usuarioService;

    // Retorna todos os usuários
    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listarTodos();
    }

    // Retorna um usuário pelo ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable UUID id) {
        return usuarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Cria um novo usuário
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Usuario usuario) {
        try {
            Usuario salvo = usuarioService.salvar(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (EmailDuplicadoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // Atualiza um usuário existente
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable UUID id, @RequestBody Usuario usuarioAtualizado) {
        try {
            return usuarioService.atualizar(id, usuarioAtualizado)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (EmailDuplicadoException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", e.getMessage()));
        }
    }

    // Remove um usuário pelo ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        return usuarioService.deletar(id)
                ? ResponseEntity.ok().<Void>build()
                : ResponseEntity.notFound().build();
    }
}