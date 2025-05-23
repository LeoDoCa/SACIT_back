package mx.edu.utez.sacit.repository;

import mx.edu.utez.sacit.model.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<RoleModel, Integer> {
    Optional<RoleModel> findByRole(String role);
    Optional<RoleModel> findByUuid(UUID uuid);
}
