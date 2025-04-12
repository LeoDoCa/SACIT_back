package mx.edu.utez.SACIT.repository;


import mx.edu.utez.SACIT.model.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface WindowRepository extends JpaRepository<Window,Integer> {
    Optional<Window> findByUuid(UUID uuid);
    @Query("SELECT MAX(w.windowNumber) FROM Window w")
    Optional<Integer> findMaxWindowNumber();

    Optional<Window> deleteByUuid(UUID uuid);
}
