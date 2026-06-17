package br.ufop.agendamento.repository;

import br.ufop.agendamento.model.Esporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface EsporteRepository extends JpaRepository<Esporte, UUID> {
}