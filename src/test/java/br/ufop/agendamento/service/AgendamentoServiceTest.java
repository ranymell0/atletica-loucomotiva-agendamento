package br.ufop.agendamento.service;

import br.ufop.agendamento.exception.AgendamentoInvalidoException;
import br.ufop.agendamento.model.Agendamento;
import br.ufop.agendamento.model.Espaco;
import br.ufop.agendamento.model.Esporte;
import br.ufop.agendamento.model.Usuario;
import br.ufop.agendamento.repository.AgendamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do AgendamentoService")
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private AgendamentoService agendamentoService;

    private Agendamento agendamento;
    private UUID agendamentoId;
    private Usuario usuario;
    private Espaco espaco;
    private Esporte esporte;

    @BeforeEach
    void setUp() {
        agendamentoId = UUID.randomUUID();
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        espaco = new Espaco();
        esporte = new Esporte();

        agendamento = new Agendamento();
        agendamento.setId(agendamentoId);
        agendamento.setUsuario(usuario);
        agendamento.setEspaco(espaco);
        agendamento.setEsporte(esporte);
        agendamento.setData(LocalDate.now().plusDays(1));
        agendamento.setHoraInicio(LocalTime.of(14, 0));
        agendamento.setHoraFim(LocalTime.of(15, 0));
        agendamento.setTipo("TREINO");
    }

    // Monta um agendamento válido "base" que os testes podem alterar um campo por vez
    private Agendamento agendamentoValido() {
        Agendamento a = new Agendamento();
        a.setUsuario(usuario);
        a.setEspaco(espaco);
        a.setEsporte(esporte);
        a.setData(LocalDate.now().plusDays(1));
        a.setHoraInicio(LocalTime.of(14, 0));
        a.setHoraFim(LocalTime.of(15, 0));
        a.setTipo("TREINO");
        return a;
    }

    @Test
    @DisplayName("Deve criar agendamento com status inicial PENDENTE")
    void deveCriarAgendamentoComStatusPendente() {
        ArgumentCaptor<Agendamento> captor = ArgumentCaptor.forClass(Agendamento.class);
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        Agendamento resultado = agendamentoService.salvar(agendamentoValido());

        verify(agendamentoRepository).save(captor.capture());
        assertEquals("PENDENTE", captor.getValue().getStatus());
        assertNotNull(resultado);
    }

    @Test
    @DisplayName("Não deve criar agendamento com data no passado")
    void naoDeveCriarAgendamentoComDataPassada() {
        Agendamento invalido = agendamentoValido();
        invalido.setData(LocalDate.now().minusDays(1));

        AgendamentoInvalidoException excecao = assertThrows(
                AgendamentoInvalidoException.class,
                () -> agendamentoService.salvar(invalido)
        );

        assertEquals("Não é possível agendar para uma data no passado.", excecao.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve permitir criar agendamento para a data de hoje")
    void devePermitirCriarAgendamentoParaHoje() {
        Agendamento valido = agendamentoValido();
        valido.setData(LocalDate.now());

        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(valido);

        Agendamento resultado = agendamentoService.salvar(valido);

        assertNotNull(resultado);
        verify(agendamentoRepository, times(1)).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Não deve criar agendamento sem data informada")
    void naoDeveCriarAgendamentoSemData() {
        Agendamento invalido = agendamentoValido();
        invalido.setData(null);

        AgendamentoInvalidoException excecao = assertThrows(
                AgendamentoInvalidoException.class,
                () -> agendamentoService.salvar(invalido)
        );

        assertEquals("A data do agendamento é obrigatória.", excecao.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Não deve criar agendamento sem usuário")
    void naoDeveCriarAgendamentoSemUsuario() {
        Agendamento invalido = agendamentoValido();
        invalido.setUsuario(null);

        AgendamentoInvalidoException excecao = assertThrows(
                AgendamentoInvalidoException.class,
                () -> agendamentoService.salvar(invalido)
        );

        assertEquals("O usuário do agendamento é obrigatório.", excecao.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Não deve criar agendamento sem espaço")
    void naoDeveCriarAgendamentoSemEspaco() {
        Agendamento invalido = agendamentoValido();
        invalido.setEspaco(null);

        AgendamentoInvalidoException excecao = assertThrows(
                AgendamentoInvalidoException.class,
                () -> agendamentoService.salvar(invalido)
        );

        assertEquals("O espaço do agendamento é obrigatório.", excecao.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Não deve criar agendamento sem esporte selecionado")
    void naoDeveCriarAgendamentoSemEsporte() {
        Agendamento invalido = agendamentoValido();
        invalido.setEsporte(null);

        AgendamentoInvalidoException excecao = assertThrows(
                AgendamentoInvalidoException.class,
                () -> agendamentoService.salvar(invalido)
        );

        assertEquals("É necessário selecionar um esporte.", excecao.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Não deve criar agendamento sem tipo informado")
    void naoDeveCriarAgendamentoSemTipo() {
        Agendamento invalido = agendamentoValido();
        invalido.setTipo(" ");

        AgendamentoInvalidoException excecao = assertThrows(
                AgendamentoInvalidoException.class,
                () -> agendamentoService.salvar(invalido)
        );

        assertEquals("O tipo do agendamento é obrigatório.", excecao.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Não deve criar agendamento sem hora de início")
    void naoDeveCriarAgendamentoSemHoraInicio() {
        Agendamento invalido = agendamentoValido();
        invalido.setHoraInicio(null);

        AgendamentoInvalidoException excecao = assertThrows(
                AgendamentoInvalidoException.class,
                () -> agendamentoService.salvar(invalido)
        );

        assertEquals("A hora de início é obrigatória.", excecao.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Não deve criar agendamento sem hora de fim")
    void naoDeveCriarAgendamentoSemHoraFim() {
        Agendamento invalido = agendamentoValido();
        invalido.setHoraFim(null);

        AgendamentoInvalidoException excecao = assertThrows(
                AgendamentoInvalidoException.class,
                () -> agendamentoService.salvar(invalido)
        );

        assertEquals("A hora de fim é obrigatória.", excecao.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Não deve criar agendamento com hora de fim anterior à hora de início")
    void naoDeveCriarAgendamentoComHoraFimAntesDoInicio() {
        Agendamento invalido = agendamentoValido();
        invalido.setHoraInicio(LocalTime.of(15, 0));
        invalido.setHoraFim(LocalTime.of(14, 0));

        AgendamentoInvalidoException excecao = assertThrows(
                AgendamentoInvalidoException.class,
                () -> agendamentoService.salvar(invalido)
        );

        assertEquals("A hora de fim deve ser posterior à hora de início.", excecao.getMessage());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Não deve criar agendamento com hora de fim igual à hora de início")
    void naoDeveCriarAgendamentoComHorasIguais() {
        Agendamento invalido = agendamentoValido();
        invalido.setHoraInicio(LocalTime.of(14, 0));
        invalido.setHoraFim(LocalTime.of(14, 0));

        assertThrows(AgendamentoInvalidoException.class,
                () -> agendamentoService.salvar(invalido));

        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve buscar agendamento por ID existente")
    void deveBuscarAgendamentoPorIdExistente() {
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));

        Optional<Agendamento> resultado = agendamentoService.buscarPorId(agendamentoId);

        assertTrue(resultado.isPresent());
        assertEquals("TREINO", resultado.get().getTipo());
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar agendamento com ID inexistente")
    void deveRetornarVazioAoBuscarIdInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(agendamentoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        Optional<Agendamento> resultado = agendamentoService.buscarPorId(idInexistente);

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve listar todos os agendamentos cadastrados")
    void deveListarTodosOsAgendamentos() {
        when(agendamentoRepository.findAll()).thenReturn(List.of(agendamento));

        List<Agendamento> resultado = agendamentoService.listarTodos();

        assertEquals(1, resultado.size());
        verify(agendamentoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve confirmar um agendamento pendente, mudando status para CONFIRMADO")
    void deveConfirmarAgendamentoPendente() {
        agendamento.setStatus("PENDENTE");
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        Optional<Agendamento> resultado = agendamentoService.confirmar(agendamentoId);

        assertTrue(resultado.isPresent());
        assertEquals("CONFIRMADO", resultado.get().getStatus());
        verify(agendamentoRepository, times(1)).save(agendamento);
    }

    @Test
    @DisplayName("Não deve confirmar agendamento inexistente")
    void naoDeveConfirmarAgendamentoInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(agendamentoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        Optional<Agendamento> resultado = agendamentoService.confirmar(idInexistente);

        assertFalse(resultado.isPresent());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve cancelar um agendamento, mudando status para CANCELADO")
    void deveCancelarAgendamento() {
        agendamento.setStatus("CONFIRMADO");
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        when(agendamentoRepository.save(any(Agendamento.class))).thenReturn(agendamento);

        Optional<Agendamento> resultado = agendamentoService.cancelar(agendamentoId);

        assertTrue(resultado.isPresent());
        assertEquals("CANCELADO", resultado.get().getStatus());
        verify(agendamentoRepository, times(1)).save(agendamento);
    }

    @Test
    @DisplayName("Não deve cancelar agendamento inexistente")
    void naoDeveCancelarAgendamentoInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(agendamentoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        Optional<Agendamento> resultado = agendamentoService.cancelar(idInexistente);

        assertFalse(resultado.isPresent());
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve deletar um agendamento existente")
    void deveDeletarAgendamentoExistente() {
        when(agendamentoRepository.findById(agendamentoId)).thenReturn(Optional.of(agendamento));
        doNothing().when(agendamentoRepository).delete(agendamento);

        boolean resultado = agendamentoService.deletar(agendamentoId);

        assertTrue(resultado);
        verify(agendamentoRepository, times(1)).delete(agendamento);
    }

    @Test
    @DisplayName("Deve retornar false ao deletar agendamento inexistente")
    void deveRetornarFalseAoDeletarInexistente() {
        UUID idInexistente = UUID.randomUUID();
        when(agendamentoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        boolean resultado = agendamentoService.deletar(idInexistente);

        assertFalse(resultado);
        verify(agendamentoRepository, never()).delete(any(Agendamento.class));
    }
}