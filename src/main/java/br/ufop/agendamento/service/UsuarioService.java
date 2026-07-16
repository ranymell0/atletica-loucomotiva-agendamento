package br.ufop.agendamento.service;

import br.ufop.agendamento.exception.EmailDuplicadoException;
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

    // Código secreto que garante o perfil de administrador no cadastro
    private static final String CODIGO_ADMIN = "LOUCOUFOP";

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

    // Autentica um usuário pelo e-mail e senha.
    // Retorna vazio se o e-mail não existir ou a senha não bater.
    public Optional<Usuario> autenticar(String email, String senha) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null || !usuario.getSenha().equals(senha)) {
            return Optional.empty();
        }
        return Optional.of(usuario);
    }

    // Salva um novo usuário, validando e-mail duplicado e decidindo o perfil no servidor
    public Usuario salvar(Usuario usuario) {
        Usuario existente = usuarioRepository.findByEmail(usuario.getEmail());
        if (existente != null) {
            throw new EmailDuplicadoException(usuario.getEmail());
        }

        // O perfil nunca é confiado a partir do que o cliente mandou;
        // é sempre decidido aqui, com base no código admin informado.
        if (CODIGO_ADMIN.equals(usuario.getCodigoAdmin())) {
            usuario.setPerfil("ADMIN");
        } else {
            usuario.setPerfil("ATLETA");
        }

        return usuarioRepository.save(usuario);
    }

    // Atualiza os dados de um usuário existente, validando e-mail duplicado
    // (ignorando o próprio usuário que está sendo atualizado)
    public Optional<Usuario> atualizar(UUID id, Usuario usuarioAtualizado) {
        return usuarioRepository.findById(id).map(usuario -> {
            Usuario usuarioComMesmoEmail = usuarioRepository.findByEmail(usuarioAtualizado.getEmail());
            if (usuarioComMesmoEmail != null && !usuarioComMesmoEmail.getId().equals(id)) {
                throw new EmailDuplicadoException(usuarioAtualizado.getEmail());
            }

            usuario.setNome(usuarioAtualizado.getNome());
            usuario.setEmail(usuarioAtualizado.getEmail());
            usuario.setSenha(usuarioAtualizado.getSenha());
            usuario.setPerfil(usuarioAtualizado.getPerfil());
            usuario.setFuncao(usuarioAtualizado.getFuncao());
            usuario.setEsportes(usuarioAtualizado.getEsportes());
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