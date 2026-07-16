package br.ufop.agendamento.service;

import br.ufop.agendamento.exception.AgendamentoInvalidoException;
import br.ufop.agendamento.model.Agendamento;
import br.ufop.agendamento.repository.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Camada de serviço responsável pelas regras de negócio dos agendamentos
@Service
public class AgendamentoService {

    // Injeção do repositório de agendamentos
    @Autowired
    private AgendamentoRepository agendamentoRepository;

    // Retorna todos os agendamentos cadastrados
    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    // Retorna um agendamento pelo ID
    public Optional<Agendamento> buscarPorId(UUID id) {
        return agendamentoRepository.findById(id);
    }

    // Salva um novo agendamento com status inicial PENDENTE, validando os dados
    public Agendamento salvar(Agendamento agendamento) {
        validarAgendamento(agendamento);
        agendamento.setStatus("PENDENTE");
        return agendamentoRepository.save(agendamento);
    }

    // Valida todas as regras de negócio de um agendamento
    private void validarAgendamento(Agendamento agendamento) {
        if (agendamento.getUsuario() == null) {
            throw new AgendamentoInvalidoException("O usuário do agendamento é obrigatório.");
        }
        if (agendamento.getEspaco() == null) {
            throw new AgendamentoInvalidoException("O espaço do agendamento é obrigatório.");
        }
        if (agendamento.getEsporte() == null) {
            throw new AgendamentoInvalidoException("É necessário selecionar um esporte.");
        }
        if (agendamento.getTipo() == null || agendamento.getTipo().isBlank()) {
            throw new AgendamentoInvalidoException("O tipo do agendamento é obrigatório.");
        }

        validarData(agendamento.getData());
        validarHorario(agendamento.getHoraInicio(), agendamento.getHoraFim());
    }

    // Valida se a data do agendamento não está no passado
    private void validarData(LocalDate data) {
        if (data == null) {
            throw new AgendamentoInvalidoException("A data do agendamento é obrigatória.");
        }
        if (data.isBefore(LocalDate.now())) {
            throw new AgendamentoInvalidoException("Não é possível agendar para uma data no passado.");
        }
    }

    // Valida se a hora de início e fim foram informadas e se fazem sentido
    private void validarHorario(java.time.LocalTime horaInicio, java.time.LocalTime horaFim) {
        if (horaInicio == null) {
            throw new AgendamentoInvalidoException("A hora de início é obrigatória.");
        }
        if (horaFim == null) {
            throw new AgendamentoInvalidoException("A hora de fim é obrigatória.");
        }
        if (!horaFim.isAfter(horaInicio)) {
            throw new AgendamentoInvalidoException("A hora de fim deve ser posterior à hora de início.");
        }
    }

    // Atualiza os dados de um agendamento existente
    public Optional<Agendamento> atualizar(UUID id, Agendamento agendamentoAtualizado) {
        return agendamentoRepository.findById(id).map(agendamento -> {
            validarAgendamento(agendamentoAtualizado);
            agendamento.setUsuario(agendamentoAtualizado.getUsuario());
            agendamento.setEspaco(agendamentoAtualizado.getEspaco());
            agendamento.setEsporte(agendamentoAtualizado.getEsporte());
            agendamento.setHorario(agendamentoAtualizado.getHorario());
            agendamento.setData(agendamentoAtualizado.getData());
            agendamento.setHoraInicio(agendamentoAtualizado.getHoraInicio());
            agendamento.setHoraFim(agendamentoAtualizado.getHoraFim());
            agendamento.setTipo(agendamentoAtualizado.getTipo());
            agendamento.setStatus(agendamentoAtualizado.getStatus());
            return agendamentoRepository.save(agendamento);
        });
    }

    // Remove um agendamento pelo ID
    public boolean deletar(UUID id) {
        return agendamentoRepository.findById(id).map(agendamento -> {
            agendamentoRepository.delete(agendamento);
            return true;
        }).orElse(false);
    }

    // Confirma um agendamento pendente
    public Optional<Agendamento> confirmar(UUID id) {
        return agendamentoRepository.findById(id).map(agendamento -> {
            agendamento.setStatus("CONFIRMADO");
            return agendamentoRepository.save(agendamento);
        });
    }

    // Cancela um agendamento
    public Optional<Agendamento> cancelar(UUID id) {
        return agendamentoRepository.findById(id).map(agendamento -> {
            agendamento.setStatus("CANCELADO");
            return agendamentoRepository.save(agendamento);
        });
    }
}