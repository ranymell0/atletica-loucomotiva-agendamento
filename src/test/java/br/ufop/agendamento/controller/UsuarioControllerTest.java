package br.ufop.agendamento.controller;

import br.ufop.agendamento.exception.EmailDuplicadoException;
import br.ufop.agendamento.model.Usuario;
import br.ufop.agendamento.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
@DisplayName("Testes do UsuarioController")
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

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
    @DisplayName("POST /usuarios - deve cadastrar usuário válido e retornar 201")
    void deveCadastrarUsuarioValido() throws Exception {
        when(usuarioService.salvar(any(Usuario.class))).thenReturn(usuario);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Rany Silva"))
                .andExpect(jsonPath("$.email").value("rany@ufop.edu.br"));
    }

    @Test
    @DisplayName("POST /usuarios - deve retornar 409 ao cadastrar com e-mail duplicado")
    void deveRetornar409AoCadastrarEmailDuplicado() throws Exception {
        when(usuarioService.salvar(any(Usuario.class)))
                .thenThrow(new EmailDuplicadoException("rany@ufop.edu.br"));

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value(
                        "Já existe um usuário cadastrado com o e-mail: rany@ufop.edu.br"));
    }

    @Test
    @DisplayName("GET /usuarios/{id} - deve retornar usuário existente")
    void deveBuscarUsuarioExistente() throws Exception {
        when(usuarioService.buscarPorId(usuarioId)).thenReturn(Optional.of(usuario));

        mockMvc.perform(get("/usuarios/{id}", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Rany Silva"));
    }

    @Test
    @DisplayName("GET /usuarios/{id} - deve retornar 404 para ID inexistente")
    void deveRetornar404ParaIdInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(usuarioService.buscarPorId(idInexistente)).thenReturn(Optional.empty());

        mockMvc.perform(get("/usuarios/{id}", idInexistente))
                .andExpect(status().isNotFound());
    }
}