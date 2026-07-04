package br.ufop.agendamento.service;

import br.ufop.agendamento.model.Horario;
import br.ufop.agendamento.repository.HorarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Camada de serviço responsável pelas regras de negócio dos horários
@Service
public class HorarioService {

    // Injeção do repositório de horários
    @Autowired
    private HorarioRepository horarioRepository;

    // Retorna todos os horários cadastrados
    public List<Horario> listarTodos() {
        return horarioRepository.findAll();
    }

    // Retorna um horário pelo ID
    public Optional<Horario> buscarPorId(UUID id) {
        return horarioRepository.findById(id);
    }

    // Salva um novo horário
    public Horario salvar(Horario horario) {
        return horarioRepository.save(horario);
    }

    // Atualiza os dados de um horário existente
    public Optional<Horario> atualizar(UUID id, Horario horarioAtualizado) {
        return horarioRepository.findById(id).map(horario -> {
            horario.setEspaco(horarioAtualizado.getEspaco());
            horario.setHoraInicio(horarioAtualizado.getHoraInicio());
            horario.setHoraFim(horarioAtualizado.getHoraFim());
            horario.setDiaSemana(horarioAtualizado.getDiaSemana());
            horario.setAtivo(horarioAtualizado.getAtivo());
            return horarioRepository.save(horario);
        });
    }

    // Remove um horário pelo ID
    public boolean deletar(UUID id) {
        return horarioRepository.findById(id).map(horario -> {
            horarioRepository.delete(horario);
            return true;
        }).orElse(false);
    }
}