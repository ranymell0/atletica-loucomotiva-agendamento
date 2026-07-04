package br.ufop.agendamento.service;

import br.ufop.agendamento.model.Usuario;
import br.ufop.agendamento.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Camada de serviço responsável pelas regras de negócio dos usuários
@Service
public class UsuarioService {

    // Injeção do repositório de usuários
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Retorna todos os usuários cadastrados
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // Retorna um usuário pelo ID
    public Optional<Usuario> buscarPorId(UUID id) {
        return usuarioRepository.findById(id);
    }

    // Salva um novo usuário
    public Usuario salvar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    // Atualiza os dados de um usuário existente
    public Optional<Usuario> atualizar(UUID id, Usuario usuarioAtualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNome(usuarioAtualizado.getNome());
            usuario.setEmail(usuarioAtualizado.getEmail());
            usuario.setSenha(usuarioAtualizado.getSenha());
            usuario.setPerfil(usuarioAtualizado.getPerfil());
            return usuarioRepository.save(usuario);
        });
    }

    // Remove um usuário pelo ID
    public boolean deletar(UUID id) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuarioRepository.delete(usuario);
            return true;
        }).orElse(false);
    }
}
