package br.ufop.agendamento.service;

import br.ufop.agendamento.model.Agendamento;
import br.ufop.agendamento.repository.AgendamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // Salva um novo agendamento com status inicial PENDENTE
    public Agendamento salvar(Agendamento agendamento) {
        agendamento.setStatus("PENDENTE");
        return agendamentoRepository.save(agendamento);
    }

    // Atualiza os dados de um agendamento existente
    public Optional<Agendamento> atualizar(UUID id, Agendamento agendamentoAtualizado) {
        return agendamentoRepository.findById(id).map(agendamento -> {
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