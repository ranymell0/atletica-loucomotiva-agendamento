package br.ufop.agendamento.service;

import br.ufop.agendamento.model.Esporte;
import br.ufop.agendamento.repository.EsporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Camada de serviço responsável pelas regras de negócio dos esportes
@Service
public class EsporteService {

    // Injeção do repositório de esportes
    @Autowired
    private EsporteRepository esporteRepository;

    // Retorna todos os esportes cadastrados
    public List<Esporte> listarTodos() {
        return esporteRepository.findAll();
    }

    // Retorna um esporte pelo ID
    public Optional<Esporte> buscarPorId(UUID id) {
        return esporteRepository.findById(id);
    }

    // Salva um novo esporte
    public Esporte salvar(Esporte esporte) {
        return esporteRepository.save(esporte);
    }

    // Atualiza os dados de um esporte existente
    public Optional<Esporte> atualizar(UUID id, Esporte esporteAtualizado) {
        return esporteRepository.findById(id).map(esporte -> {
            esporte.setNome(esporteAtualizado.getNome());
            esporte.setDescricao(esporteAtualizado.getDescricao());
            return esporteRepository.save(esporte);
        });
    }

    // Remove um esporte pelo ID
    public boolean deletar(UUID id) {
        return esporteRepository.findById(id).map(esporte -> {
            esporteRepository.delete(esporte);
            return true;
        }).orElse(false);
    }
}