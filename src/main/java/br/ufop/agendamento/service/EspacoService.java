package br.ufop.agendamento.service;

import br.ufop.agendamento.model.Espaco;
import br.ufop.agendamento.repository.EspacoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Camada de serviço responsável pelas regras de negócio dos espaços
@Service
public class EspacoService {

    // Injeção do repositório de espaços
    @Autowired
    private EspacoRepository espacoRepository;

    // Retorna todos os espaços cadastrados
    public List<Espaco> listarTodos() {
        return espacoRepository.findAll();
    }

    // Retorna um espaço pelo ID
    public Optional<Espaco> buscarPorId(UUID id) {
        return espacoRepository.findById(id);
    }

    // Salva um novo espaço
    public Espaco salvar(Espaco espaco) {
        return espacoRepository.save(espaco);
    }

    // Atualiza os dados de um espaço existente
    public Optional<Espaco> atualizar(UUID id, Espaco espacoAtualizado) {
        return espacoRepository.findById(id).map(espaco -> {
            espaco.setNome(espacoAtualizado.getNome());
            espaco.setDescricao(espacoAtualizado.getDescricao());
            espaco.setCapacidadeMaxima(espacoAtualizado.getCapacidadeMaxima());
            espaco.setAtivo(espacoAtualizado.getAtivo());
            return espacoRepository.save(espaco);
        });
    }

    // Remove um espaço pelo ID
    public boolean deletar(UUID id) {
        return espacoRepository.findById(id).map(espaco -> {
            espacoRepository.delete(espaco);
            return true;
        }).orElse(false);
    }
}