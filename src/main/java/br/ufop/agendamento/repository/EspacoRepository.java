package br.ufop.agendamento.repository;

import br.ufop.agendamento.model.Espaco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface EspacoRepository extends JpaRepository<Espaco, UUID> {
}