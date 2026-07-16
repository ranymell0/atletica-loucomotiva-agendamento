package br.ufop.agendamento.controller;

import br.ufop.agendamento.repository.AgendamentoRepository;
import br.ufop.agendamento.repository.EspacoRepository;
import br.ufop.agendamento.repository.EsporteRepository;
import br.ufop.agendamento.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Testes de sistema: encadeiam várias requisições HTTP reais, simulando o
// uso do sistema por uma pessoa de ponta a ponta, batendo no Postgres real.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Testes de Sistema - Fluxo completo")
class SistemaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EspacoRepository espacoRepository;

    @Autowired
    private EsporteRepository esporteRepository;

    @Autowired
    private AgendamentoRepository agendamentoRepository;

    @BeforeEach
    void limparBanco() {
        agendamentoRepository.deleteAll();
        usuarioRepository.deleteAll();
        espacoRepository.deleteAll();
        esporteRepository.deleteAll();
    }

    @Test
    @DisplayName("Fluxo 1: Atleta se cadastra, faz login, agenda e vê no dashboard")
    void fluxoCompletoAtletaCadastroLoginEAgendamento() throws Exception {
        // 1) Um espaço e um esporte precisam já existir (normalmente cadastrados por um admin)
        Map<String, Object> espacoJson = Map.of(
                "nome", "Quadra Poliesportiva",
                "descricao", "Quadra coberta",
                "capacidadeMaxima", 30,
                "ativo", true
        );
        MvcResult espacoResult = mockMvc.perform(post("/espacos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(espacoJson)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String espacoId = objectMapper.readTree(espacoResult.getResponse().getContentAsString()).get("id").asText();

        Map<String, Object> esporteJson = Map.of(
                "nome", "Futsal",
                "descricao", "Futebol de salão"
        );
        MvcResult esporteResult = mockMvc.perform(post("/esportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(esporteJson)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String esporteId = objectMapper.readTree(esporteResult.getResponse().getContentAsString()).get("id").asText();

        // 2) Atleta se cadastra
        Map<String, Object> cadastroJson = Map.of(
                "nome", "Rany Silva",
                "email", "rany.sistema@ufop.edu.br",
                "senha", "senha123"
        );
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadastroJson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("ATLETA"));

        // 3) Atleta faz login
        Map<String, Object> loginJson = Map.of(
                "email", "rany.sistema@ufop.edu.br",
                "senha", "senha123"
        );
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginJson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andReturn();
        JsonNode usuarioLogado = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String usuarioId = usuarioLogado.get("id").asText();

        // 4) Atleta realiza um agendamento usando os IDs reais obtidos acima
        Map<String, Object> agendamentoJson = Map.of(
                "usuario", Map.of("id", usuarioId),
                "espaco", Map.of("id", espacoId),
                "esporte", Map.of("id", esporteId),
                "data", LocalDate.now().plusDays(1).toString(),
                "horaInicio", "14:00:00",
                "horaFim", "15:00:00",
                "tipo", "TREINO"
        );
        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoJson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        // 5) O agendamento aparece na listagem geral (o "dashboard" consome esse endpoint)
        mockMvc.perform(get("/agendamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].usuario.id").value(usuarioId))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));

        // Confirma também direto no banco, sem depender só da resposta HTTP
        Assertions.assertEquals(1, agendamentoRepository.findAll().size());
    }

    @Test
    @DisplayName("Fluxo 2: Admin faz login, confirma agendamento e atleta vê status CONFIRMADO")
    void fluxoCompletoAdminConfirmaEAtletaVeStatusAtualizado() throws Exception {
        // 1) Espaço, esporte e atleta já existentes (pré-condição do cenário)
        Map<String, Object> espacoJson = Map.of(
                "nome", "Quadra Poliesportiva",
                "descricao", "Quadra coberta",
                "capacidadeMaxima", 30,
                "ativo", true
        );
        MvcResult espacoResult = mockMvc.perform(post("/espacos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(espacoJson)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String espacoId = objectMapper.readTree(espacoResult.getResponse().getContentAsString()).get("id").asText();

        Map<String, Object> esporteJson = Map.of("nome", "Futsal", "descricao", "Futebol de salão");
        MvcResult esporteResult = mockMvc.perform(post("/esportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(esporteJson)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String esporteId = objectMapper.readTree(esporteResult.getResponse().getContentAsString()).get("id").asText();

        Map<String, Object> atletaJson = Map.of(
                "nome", "Rany Silva",
                "email", "rany.fluxo2@ufop.edu.br",
                "senha", "senha123"
        );
        MvcResult atletaResult = mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atletaJson)))
                .andExpect(status().isCreated())
                .andReturn();
        String atletaId = objectMapper.readTree(atletaResult.getResponse().getContentAsString()).get("id").asText();

        // 2) Admin se cadastra usando o código admin correto
        Map<String, Object> adminJson = Map.of(
                "nome", "Admin Loucomotiva",
                "email", "admin.fluxo2@ufop.edu.br",
                "senha", "senhaAdmin123",
                "codigoAdmin", "LOUCOUFOP"
        );
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminJson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.perfil").value("ADMIN"));

        // 3) Admin faz login
        Map<String, Object> loginAdminJson = Map.of(
                "email", "admin.fluxo2@ufop.edu.br",
                "senha", "senhaAdmin123"
        );
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginAdminJson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.perfil").value("ADMIN"));

        // 4) O atleta já tem um agendamento PENDENTE criado previamente
        Map<String, Object> agendamentoJson = Map.of(
                "usuario", Map.of("id", atletaId),
                "espaco", Map.of("id", espacoId),
                "esporte", Map.of("id", esporteId),
                "data", LocalDate.now().plusDays(1).toString(),
                "horaInicio", "16:00:00",
                "horaFim", "17:00:00",
                "tipo", "JOGO"
        );
        MvcResult agendamentoResult = mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoJson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn();
        String agendamentoId = objectMapper.readTree(agendamentoResult.getResponse().getContentAsString())
                .get("id").asText();

        // 5) Admin confirma o agendamento
        mockMvc.perform(patch("/agendamentos/{id}/confirmar", agendamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        // 6) Atleta consulta o próprio agendamento e vê o novo status
        mockMvc.perform(get("/agendamentos/{id}", agendamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));

        // Confirma também direto no banco
        Assertions.assertEquals("CONFIRMADO",
                agendamentoRepository.findById(java.util.UUID.fromString(agendamentoId)).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("Fluxo 3: Admin cadastra espaço e horário, e o atleta consegue selecioná-lo no agendamento")
    void fluxoCompletoAdminCadastraEspacoEHorarioEAtletaAgenda() throws Exception {
        // 1) Admin cadastra um novo espaço
        Map<String, Object> espacoJson = Map.of(
                "nome", "Quadra de Vôlei",
                "descricao", "Quadra externa de vôlei de praia",
                "capacidadeMaxima", 12,
                "ativo", true
        );
        MvcResult espacoResult = mockMvc.perform(post("/espacos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(espacoJson)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String espacoId = objectMapper.readTree(espacoResult.getResponse().getContentAsString()).get("id").asText();

        // 2) Admin cadastra um horário vinculado a esse espaço
        Map<String, Object> horarioJson = Map.of(
                "espaco", Map.of("id", espacoId),
                "horaInicio", "18:00:00",
                "horaFim", "19:00:00",
                "diaSemana", "SEGUNDA",
                "ativo", true
        );
        MvcResult horarioResult = mockMvc.perform(post("/horarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(horarioJson)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.diaSemana").value("SEGUNDA"))
                .andReturn();
        String horarioId = objectMapper.readTree(horarioResult.getResponse().getContentAsString()).get("id").asText();

        // 3) O atleta consulta os horários disponíveis (como o frontend faria antes de montar o formulário)
        mockMvc.perform(get("/horarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(horarioId))
                .andExpect(jsonPath("$[0].espaco.id").value(espacoId));

        // 4) Esporte e atleta necessários para o agendamento
        Map<String, Object> esporteJson = Map.of("nome", "Vôlei", "descricao", "Vôlei de praia");
        MvcResult esporteResult = mockMvc.perform(post("/esportes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(esporteJson)))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        String esporteId = objectMapper.readTree(esporteResult.getResponse().getContentAsString()).get("id").asText();

        Map<String, Object> atletaJson = Map.of(
                "nome", "Rany Silva",
                "email", "rany.fluxo3@ufop.edu.br",
                "senha", "senha123"
        );
        MvcResult atletaResult = mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(atletaJson)))
                .andExpect(status().isCreated())
                .andReturn();
        String atletaId = objectMapper.readTree(atletaResult.getResponse().getContentAsString()).get("id").asText();

        // 5) Atleta consegue criar o agendamento referenciando o horário cadastrado pelo admin
        Map<String, Object> agendamentoJson = Map.of(
                "usuario", Map.of("id", atletaId),
                "espaco", Map.of("id", espacoId),
                "esporte", Map.of("id", esporteId),
                "horario", Map.of("id", horarioId),
                "data", LocalDate.now().plusDays(2).toString(),
                "horaInicio", "18:00:00",
                "horaFim", "19:00:00",
                "tipo", "TREINO"
        );
        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoJson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.horario.id").value(horarioId));

        Assertions.assertEquals(1, agendamentoRepository.findAll().size());
    }
}