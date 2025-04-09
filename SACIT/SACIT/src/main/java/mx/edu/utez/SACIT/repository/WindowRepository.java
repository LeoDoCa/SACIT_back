package mx.edu.utez.SACIT.repository;


import mx.edu.utez.SACIT.model.Window;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WindowRepository extends JpaRepository<Window,Integer> {
    Optional<Window> findByUuid(UUID uuid);
}
