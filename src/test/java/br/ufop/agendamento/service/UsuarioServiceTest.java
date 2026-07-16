package br.ufop.agendamento.service;

import br.ufop.agendamento.exception.EmailDuplicadoException;
import br.ufop.agendamento.model.Usuario;
import br.ufop.agendamento.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private UUID usuarioId;

    @BeforeEach
    void setUp() {
        usuarioId = UUID.randomUUID();
        usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNome("Rany Silva");
        usuario.setEmail("rany@ufop.edu.br");
        usuario.setSenha("senha123");
        usuario.setPerfil("ATLETA");
        usuario.setFuncao("Atleta");
    }

    @Test
    @DisplayName("Deve listar todos os usuários cadastrados")
    void deveListarTodosOsUsuarios() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(UUID.randomUUID());
        outroUsuario.setNome("João Admin");

        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario, outroUsuario));

        List<Usuario> resultado = usuarioService.listarTodos();

        assertEquals(2, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há usuários cadastrados")
    void deveRetornarListaVaziaQuandoNaoHaUsuarios() {
        when(usuarioRepository.findAll()).thenReturn(List.of());

        List<Usuario> resultado = usuarioService.listarTodos();

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve buscar usuário por ID existente")
    void deveBuscarUsuarioPorIdExistente() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorId(usuarioId);

        assertTrue(resultado.isPresent());
        assertEquals("Rany Silva", resultado.get().getNome());
        assertEquals("rany@ufop.edu.br", resultado.get().getEmail());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar ID inexistente")
    void deveRetornarVazioAoBuscarIdInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.buscarPorId(idInexistente);

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve salvar um novo usuário com sucesso quando e-mail não existe")
    void deveSalvarNovoUsuarioComSucesso() {
        when(usuarioRepository.findByEmail(usuario.getEmail())).thenReturn(null);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Usuario resultado = usuarioService.salvar(usuario);

        assertNotNull(resultado);
        assertEquals("Rany Silva", resultado.getNome());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Não deve salvar usuário com e-mail já cadastrado")
    void naoDeveSalvarUsuarioComEmailDuplicado() {
        Usuario usuarioExistente = new Usuario();
        usuarioExistente.setId(UUID.randomUUID());
        usuarioExistente.setEmail("rany@ufop.edu.br");

        when(usuarioRepository.findByEmail("rany@ufop.edu.br")).thenReturn(usuarioExistente);

        EmailDuplicadoException excecao = assertThrows(
                EmailDuplicadoException.class,
                () -> usuarioService.salvar(usuario)
        );

        assertTrue(excecao.getMessage().contains("rany@ufop.edu.br"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve atribuir perfil ADMIN quando o código admin correto é informado")
    void deveAtribuirPerfilAdminComCodigoCorreto() {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome("Admin Teste");
        novoUsuario.setEmail("admin@ufop.edu.br");
        novoUsuario.setSenha("senha123");
        novoUsuario.setCodigoAdmin("LOUCOUFOP");

        when(usuarioRepository.findByEmail(novoUsuario.getEmail())).thenReturn(null);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.salvar(novoUsuario);

        assertEquals("ADMIN", resultado.getPerfil());
    }

    @Test
    @DisplayName("Deve atribuir perfil ATLETA quando o código admin está errado")
    void deveAtribuirPerfilAtletaComCodigoErrado() {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome("Atleta Teste");
        novoUsuario.setEmail("atleta.codigo.errado@ufop.edu.br");
        novoUsuario.setSenha("senha123");
        novoUsuario.setCodigoAdmin("codigoQualquer");

        when(usuarioRepository.findByEmail(novoUsuario.getEmail())).thenReturn(null);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.salvar(novoUsuario);

        assertEquals("ATLETA", resultado.getPerfil());
    }

    @Test
    @DisplayName("Deve atribuir perfil ATLETA quando nenhum código admin é informado")
    void deveAtribuirPerfilAtletaSemCodigoAdmin() {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome("Atleta Sem Codigo");
        novoUsuario.setEmail("atleta.sem.codigo@ufop.edu.br");
        novoUsuario.setSenha("senha123");
        // codigoAdmin não informado (null)

        when(usuarioRepository.findByEmail(novoUsuario.getEmail())).thenReturn(null);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.salvar(novoUsuario);

        assertEquals("ATLETA", resultado.getPerfil());
    }

    @Test
    @DisplayName("Deve ignorar o perfil enviado pelo cliente e decidir com base no código admin")
    void deveIgnorarPerfilEnviadoPeloCliente() {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome("Tentativa de Burlar");
        novoUsuario.setEmail("tentativa@ufop.edu.br");
        novoUsuario.setSenha("senha123");
        novoUsuario.setPerfil("ADMIN"); // tenta se auto-promover a admin
        // sem código admin correto

        when(usuarioRepository.findByEmail(novoUsuario.getEmail())).thenReturn(null);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.salvar(novoUsuario);

        assertEquals("ATLETA", resultado.getPerfil());
    }

    @Test
    @DisplayName("Deve atualizar dados de um usuário existente")
    void deveAtualizarUsuarioExistente() {
        Usuario dadosAtualizados = new Usuario();
        dadosAtualizados.setNome("Rany Silva Atualizado");
        dadosAtualizados.setEmail("rany.nova@ufop.edu.br");
        dadosAtualizados.setSenha("novaSenha");
        dadosAtualizados.setPerfil("ADMIN");
        dadosAtualizados.setFuncao("Coordenadora");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail("rany.nova@ufop.edu.br")).thenReturn(null);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Optional<Usuario> resultado = usuarioService.atualizar(usuarioId, dadosAtualizados);

        assertTrue(resultado.isPresent());
        assertEquals("Rany Silva Atualizado", resultado.get().getNome());
        assertEquals("ADMIN", resultado.get().getPerfil());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Deve permitir atualizar usuário mantendo o próprio e-mail")
    void devePermitirAtualizarUsuarioMantendoProprioEmail() {
        Usuario dadosAtualizados = new Usuario();
        dadosAtualizados.setNome("Rany Silva");
        dadosAtualizados.setEmail("rany@ufop.edu.br");
        dadosAtualizados.setSenha("senha123");
        dadosAtualizados.setPerfil("ATLETA");
        dadosAtualizados.setFuncao("Atleta");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail("rany@ufop.edu.br")).thenReturn(usuario);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        Optional<Usuario> resultado = usuarioService.atualizar(usuarioId, dadosAtualizados);

        assertTrue(resultado.isPresent());
        verify(usuarioRepository, times(1)).save(usuario);
    }

    @Test
    @DisplayName("Não deve atualizar usuário para um e-mail já usado por outra pessoa")
    void naoDeveAtualizarParaEmailDeOutroUsuario() {
        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(UUID.randomUUID());
        outroUsuario.setEmail("outro@ufop.edu.br");

        Usuario dadosAtualizados = new Usuario();
        dadosAtualizados.setEmail("outro@ufop.edu.br");

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByEmail("outro@ufop.edu.br")).thenReturn(outroUsuario);

        assertThrows(EmailDuplicadoException.class,
                () -> usuarioService.atualizar(usuarioId, dadosAtualizados));

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Não deve atualizar usuário inexistente")
    void naoDeveAtualizarUsuarioInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.atualizar(idInexistente, usuario);

        assertFalse(resultado.isPresent());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve deletar um usuário existente e retornar true")
    void deveDeletarUsuarioExistente() {
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        doNothing().when(usuarioRepository).delete(usuario);

        boolean resultado = usuarioService.deletar(usuarioId);

        assertTrue(resultado);
        verify(usuarioRepository, times(1)).delete(usuario);
    }

    @Test
    @DisplayName("Deve retornar false ao tentar deletar usuário inexistente")
    void deveRetornarFalseAoDeletarUsuarioInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(usuarioRepository.findById(idInexistente)).thenReturn(Optional.empty());

        boolean resultado = usuarioService.deletar(idInexistente);

        assertFalse(resultado);
        verify(usuarioRepository, never()).delete(any(Usuario.class));
    }
}