package mx.edu.utez.sacit.repository;


import mx.edu.utez.sacit.model.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface WindowRepository extends JpaRepository<Window,Integer> {
    Optional<Window> findByUuid(UUID uuid);
    @Query("SELECT MAX(w.windowNumber) FROM Window w")
    Optional<Integer> findMaxWindowNumber();

    Optional<Window> deleteByUuid(UUID uuid);
    List<Window> findByStatus (String status);

   Window findByAttendant_Uuid(UUID uuid);
}
