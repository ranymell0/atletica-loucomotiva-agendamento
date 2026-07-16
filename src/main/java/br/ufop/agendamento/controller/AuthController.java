package br.ufop.agendamento.controller;

import br.ufop.agendamento.dto.LoginRequest;
import br.ufop.agendamento.model.Usuario;
import br.ufop.agendamento.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

// Controller responsável pela autenticação de usuários
@RestController
public class AuthController {

    // Injeção do serviço de usuários
    @Autowired
    private UsuarioService usuarioService;

    // Autentica um usuário pelo e-mail e senha
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<Usuario> usuarioAutenticado =
                usuarioService.autenticar(loginRequest.getEmail(), loginRequest.getSenha());

        if (usuarioAutenticado.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "E-mail ou senha incorretos."));
        }

        Usuario usuario = usuarioAutenticado.get();

        // Monta a resposta manualmente, sem tocar na entidade gerenciada pelo JPA
        // (mutar um campo NOT NULL numa entidade gerenciada, mesmo só pra resposta,
        // pode ser persistido de volta no banco no próximo flush automático)
        Map<String, Object> resposta = new java.util.HashMap<>();
        resposta.put("id", usuario.getId());
        resposta.put("nome", usuario.getNome());
        resposta.put("email", usuario.getEmail());
        resposta.put("perfil", usuario.getPerfil());
        resposta.put("funcao", usuario.getFuncao());

        return ResponseEntity.ok(resposta);
    }
}