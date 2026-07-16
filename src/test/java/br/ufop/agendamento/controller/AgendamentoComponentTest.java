package br.ufop.agendamento.controller;

import br.ufop.agendamento.model.Agendamento;
import br.ufop.agendamento.model.Espaco;
import br.ufop.agendamento.model.Esporte;
import br.ufop.agendamento.model.Usuario;
import br.ufop.agendamento.repository.AgendamentoRepository;
import br.ufop.agendamento.repository.EspacoRepository;
import br.ufop.agendamento.repository.EsporteRepository;
import br.ufop.agendamento.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Teste de componente: sobe o contexto Spring completo e bate no Postgres de teste real
// (Controller -> Service -> Repository -> Banco), sem mockar nada.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Componente - Agendamento")
class AgendamentoComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EspacoRepository espacoRepository;

    @Autowired
    private EsporteRepository esporteRepository;

    private Usuario usuario;
    private Espaco espaco;
    private Esporte esporte;

    @BeforeEach
    void prepararDados() {
        agendamentoRepository.deleteAll();
        usuarioRepository.deleteAll();
        espacoRepository.deleteAll();
        esporteRepository.deleteAll();

        usuario = new Usuario();
        usuario.setNome("Rany Silva");
        usuario.setEmail("rany.agendamento@ufop.edu.br");
        usuario.setSenha("senha123");
        usuario.setPerfil("ATLETA");
        usuario.setFuncao("Atleta");
        usuario = usuarioRepository.save(usuario);

        espaco = new Espaco();
        espaco.setNome("Quadra Poliesportiva");
        espaco.setDescricao("Quadra coberta do campus");
        espaco.setCapacidadeMaxima(30);
        espaco = espacoRepository.save(espaco);

        esporte = new Esporte();
        esporte.setNome("Futsal");
        esporte.setDescricao("Futebol de salão");
        esporte = esporteRepository.save(esporte);
    }

    // Monta o JSON de criação manualmente, referenciando os IDs reais já persistidos
    private Map<String, Object> agendamentoValidoJson() {
        return Map.of(
                "usuario", Map.of("id", usuario.getId().toString()),
                "espaco", Map.of("id", espaco.getId().toString()),
                "esporte", Map.of("id", esporte.getId().toString()),
                "data", LocalDate.now().plusDays(1).toString(),
                "horaInicio", "14:00:00",
                "horaFim", "15:00:00",
                "tipo", "TREINO"
        );
    }

    @Test
    @DisplayName("Deve criar agendamento válido e persistir de verdade no banco com status PENDENTE")
    void deveCriarAgendamentoEPersistirNoBanco() throws Exception {
        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoValidoJson())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.tipo").value("TREINO"));

        Assertions.assertEquals(1, agendamentoRepository.findAll().size());
        Agendamento salvo = agendamentoRepository.findAll().get(0);
        Assertions.assertEquals("PENDENTE", salvo.getStatus());
        Assertions.assertEquals(usuario.getId(), salvo.getUsuario().getId());
    }

    @Test
    @DisplayName("Deve rejeitar criação de agendamento com data no passado")
    void deveRejeitarAgendamentoComDataPassada() throws Exception {
        Map<String, Object> agendamentoInvalido = Map.of(
                "usuario", Map.of("id", usuario.getId().toString()),
                "espaco", Map.of("id", espaco.getId().toString()),
                "esporte", Map.of("id", esporte.getId().toString()),
                "data", LocalDate.now().minusDays(1).toString(),
                "horaInicio", "14:00:00",
                "horaFim", "15:00:00",
                "tipo", "TREINO"
        );

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Não é possível agendar para uma data no passado."));

        Assertions.assertEquals(0, agendamentoRepository.findAll().size());
    }

    @Test
    @DisplayName("Deve confirmar um agendamento pendente persistido no banco")
    void deveConfirmarAgendamentoPersistidoNoBanco() throws Exception {
        Agendamento agendamento = new Agendamento();
        agendamento.setUsuario(usuario);
        agendamento.setEspaco(espaco);
        agendamento.setEsporte(esporte);
        agendamento.setData(LocalDate.now().plusDays(1));
        agendamento.setHoraInicio(LocalTime.of(14, 0));
        agendamento.setHoraFim(LocalTime.of(15, 0));
        agendamento.setTipo("TREINO");
        agendamento.setStatus("PENDENTE");
        agendamento = agendamentoRepository.save(agendamento);

        mockMvc.perform(patch("/agendamentos/{id}/confirmar", agendamento.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        Agendamento atualizado = agendamentoRepository.findById(agendamento.getId()).orElseThrow();
        Assertions.assertEquals("CONFIRMADO", atualizado.getStatus());
    }

    @Test
    @DisplayName("Deve cancelar um agendamento persistido no banco")
    void deveCancelarAgendamentoPersistidoNoBanco() throws Exception {
        Agendamento agendamento = new Agendamento();
        agendamento.setUsuario(usuario);
        agendamento.setEspaco(espaco);
        agendamento.setEsporte(esporte);
        agendamento.setData(LocalDate.now().plusDays(1));
        agendamento.setHoraInicio(LocalTime.of(14, 0));
        agendamento.setHoraFim(LocalTime.of(15, 0));
        agendamento.setTipo("TREINO");
        agendamento.setStatus("CONFIRMADO");
        agendamento = agendamentoRepository.save(agendamento);

        mockMvc.perform(patch("/agendamentos/{id}/cancelar", agendamento.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));

        Agendamento atualizado = agendamentoRepository.findById(agendamento.getId()).orElseThrow();
        Assertions.assertEquals("CANCELADO", atualizado.getStatus());
    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar confirmar agendamento inexistente no banco")
    void deveRetornar404AoConfirmarInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();

        mockMvc.perform(patch("/agendamentos/{id}/confirmar", idInexistente))
                .andExpect(status().isNotFound());
    }
}