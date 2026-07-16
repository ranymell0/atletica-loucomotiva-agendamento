package br.ufop.agendamento.controller;

import br.ufop.agendamento.exception.AgendamentoInvalidoException;
import br.ufop.agendamento.model.Agendamento;
import br.ufop.agendamento.service.AgendamentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AgendamentoController.class)
@DisplayName("Testes do AgendamentoController")
class AgendamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AgendamentoService agendamentoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Agendamento agendamento;
    private UUID agendamentoId;

    @BeforeEach
    void setUp() {
        agendamentoId = UUID.randomUUID();
        agendamento = new Agendamento();
        agendamento.setId(agendamentoId);
        agendamento.setData(LocalDate.now().plusDays(1));
        agendamento.setHoraInicio(LocalTime.of(14, 0));
        agendamento.setHoraFim(LocalTime.of(15, 0));
        agendamento.setTipo("TREINO");
        agendamento.setStatus("PENDENTE");
    }

    @Test
    @DisplayName("POST /agendamentos - deve criar agendamento válido e retornar 201 com status PENDENTE")
    void deveCriarAgendamentoValido() throws Exception {
        when(agendamentoService.salvar(any(Agendamento.class))).thenReturn(agendamento);

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamento)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("TREINO"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    @DisplayName("POST /agendamentos - deve retornar 400 ao criar agendamento com data passada")
    void deveRetornar400AoCriarComDataPassada() throws Exception {
        Agendamento agendamentoDataPassada = new Agendamento();
        agendamentoDataPassada.setData(LocalDate.now().minusDays(1));
        agendamentoDataPassada.setHoraInicio(LocalTime.of(14, 0));
        agendamentoDataPassada.setHoraFim(LocalTime.of(15, 0));
        agendamentoDataPassada.setTipo("TREINO");

        when(agendamentoService.salvar(any(Agendamento.class)))
                .thenThrow(new AgendamentoInvalidoException("Não é possível agendar para uma data no passado."));

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoDataPassada)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Não é possível agendar para uma data no passado."));
    }

    @Test
    @DisplayName("POST /agendamentos - deve retornar 400 quando hora de fim é anterior à hora de início")
    void deveRetornar400ParaHorarioInvalido() throws Exception {
        Agendamento agendamentoHorarioInvalido = new Agendamento();
        agendamentoHorarioInvalido.setData(LocalDate.now().plusDays(1));
        agendamentoHorarioInvalido.setHoraInicio(LocalTime.of(15, 0));
        agendamentoHorarioInvalido.setHoraFim(LocalTime.of(14, 0));
        agendamentoHorarioInvalido.setTipo("TREINO");

        when(agendamentoService.salvar(any(Agendamento.class)))
                .thenThrow(new AgendamentoInvalidoException("A hora de fim deve ser posterior à hora de início."));

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoHorarioInvalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("A hora de fim deve ser posterior à hora de início."));
    }

    @Test
    @DisplayName("POST /agendamentos - deve retornar 400 quando esporte não é informado")
    void deveRetornar400QuandoEsporteNaoInformado() throws Exception {
        Agendamento agendamentoSemEsporte = new Agendamento();
        agendamentoSemEsporte.setData(LocalDate.now().plusDays(1));
        agendamentoSemEsporte.setHoraInicio(LocalTime.of(14, 0));
        agendamentoSemEsporte.setHoraFim(LocalTime.of(15, 0));
        agendamentoSemEsporte.setTipo("TREINO");

        when(agendamentoService.salvar(any(Agendamento.class)))
                .thenThrow(new AgendamentoInvalidoException("É necessário selecionar um esporte."));

        mockMvc.perform(post("/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(agendamentoSemEsporte)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("É necessário selecionar um esporte."));
    }

    @Test
    @DisplayName("PATCH /agendamentos/{id}/confirmar - deve confirmar e retornar 200 com status CONFIRMADO")
    void deveConfirmarAgendamento() throws Exception {
        agendamento.setStatus("CONFIRMADO");
        when(agendamentoService.confirmar(agendamentoId)).thenReturn(Optional.of(agendamento));

        mockMvc.perform(patch("/agendamentos/{id}/confirmar", agendamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"));
    }

    @Test
    @DisplayName("PATCH /agendamentos/{id}/confirmar - deve retornar 404 para agendamento inexistente")
    void deveRetornar404AoConfirmarInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(agendamentoService.confirmar(idInexistente)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/agendamentos/{id}/confirmar", idInexistente))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /agendamentos/{id}/cancelar - deve cancelar e retornar 200 com status CANCELADO")
    void deveCancelarAgendamento() throws Exception {
        agendamento.setStatus("CANCELADO");
        when(agendamentoService.cancelar(agendamentoId)).thenReturn(Optional.of(agendamento));

        mockMvc.perform(patch("/agendamentos/{id}/cancelar", agendamentoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    @DisplayName("PATCH /agendamentos/{id}/cancelar - deve retornar 404 para agendamento inexistente")
    void deveRetornar404AoCancelarInexistente() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(agendamentoService.cancelar(idInexistente)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/agendamentos/{id}/cancelar", idInexistente))
                .andExpect(status().isNotFound());
    }
}