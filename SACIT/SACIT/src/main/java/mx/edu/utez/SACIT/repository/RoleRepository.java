package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleModel, Integer> {
    RoleModel findByName(String name);
}
