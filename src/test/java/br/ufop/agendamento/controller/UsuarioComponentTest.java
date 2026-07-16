package br.ufop.agendamento.controller;

import br.ufop.agendamento.model.Usuario;
import br.ufop.agendamento.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Teste de componente: sobe o contexto Spring completo e bate no Postgres de teste real
// (Controller -> Service -> Repository -> Banco), sem mockar nada.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // faz rollback automático ao final de cada teste, mantendo o banco limpo
@DisplayName("Testes de Componente - Usuario")
class UsuarioComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void limparBanco() {
        usuarioRepository.deleteAll();
    }

    @Test
    @DisplayName("Deve cadastrar usuário e persistir de verdade no banco")
    void deveCadastrarUsuarioEPersistirNoBanco() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Rany Silva");
        usuario.setEmail("rany.componente@ufop.edu.br");
        usuario.setSenha("senha123");
        usuario.setPerfil("ATLETA");
        usuario.setFuncao("Atleta");

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("rany.componente@ufop.edu.br"));

        // Confere diretamente no banco, sem passar pelo Controller
        Usuario salvo = usuarioRepository.findByEmail("rany.componente@ufop.edu.br");
        org.junit.jupiter.api.Assertions.assertNotNull(salvo);
        org.junit.jupiter.api.Assertions.assertEquals("Rany Silva", salvo.getNome());
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com e-mail já existente no banco")
    void deveRejeitarCadastroComEmailJaExistenteNoBanco() throws Exception {
        Usuario existente = new Usuario();
        existente.setNome("João Admin");
        existente.setEmail("duplicado@ufop.edu.br");
        existente.setSenha("senha123");
        existente.setPerfil("ADMIN");
        existente.setFuncao("Coordenador");
        usuarioRepository.save(existente);

        Usuario novoComMesmoEmail = new Usuario();
        novoComMesmoEmail.setNome("Outra Pessoa");
        novoComMesmoEmail.setEmail("duplicado@ufop.edu.br");
        novoComMesmoEmail.setSenha("outrasenha");
        novoComMesmoEmail.setPerfil("ATLETA");
        novoComMesmoEmail.setFuncao("Atleta");

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoComMesmoEmail)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(
                        "Já existe um usuário cadastrado com o e-mail: duplicado@ufop.edu.br"));

        // Garante que não criou um segundo usuário com o mesmo e-mail
        long total = usuarioRepository.findAll().stream()
                .filter(u -> u.getEmail().equals("duplicado@ufop.edu.br"))
                .count();
        org.junit.jupiter.api.Assertions.assertEquals(1, total);
    }

    @Test
    @DisplayName("Deve buscar usuário existente pelo ID real no banco")
    void deveBuscarUsuarioExistentePeloIdReal() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setNome("Rany Silva");
        usuario.setEmail("busca.componente@ufop.edu.br");
        usuario.setSenha("senha123");
        usuario.setPerfil("ATLETA");
        usuario.setFuncao("Atleta");
        Usuario salvo = usuarioRepository.save(usuario);

        mockMvc.perform(get("/usuarios/{id}", salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rany Silva"));
    }

    @Test
    @DisplayName("Deve retornar 404 para ID que não existe no banco")
    void deveRetornar404ParaIdInexistenteNoBanco() throws Exception {
        UUID idInexistente = UUID.randomUUID();

        mockMvc.perform(get("/usuarios/{id}", idInexistente))
                .andExpect(status().isNotFound());
    }
}