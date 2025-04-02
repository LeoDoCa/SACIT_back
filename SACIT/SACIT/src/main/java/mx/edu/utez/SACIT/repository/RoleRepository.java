package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleModel, Integer> {
    Optional<RoleModel> findByRole(String role);
}
