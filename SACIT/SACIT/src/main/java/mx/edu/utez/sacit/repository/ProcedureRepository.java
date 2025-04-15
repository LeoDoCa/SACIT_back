package mx.edu.utez.sacit.repository;

import mx.edu.utez.sacit.model.Procedures;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProcedureRepository extends JpaRepository<Procedures, Integer> {
    Optional<Procedures> findByUuid(UUID uuid);
}