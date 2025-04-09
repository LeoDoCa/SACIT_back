package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.Window;
import mx.edu.utez.SACIT.model.WindowSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WindowScheduleRepository extends JpaRepository<WindowSchedule, Integer> {
    Optional<WindowSchedule> findByUuid(UUID uuid);
    List<WindowSchedule> findByWindow(Window window);
    List<WindowSchedule> findByDayWeek(Integer dayWeek);
    List<WindowSchedule> findByWindowAndDayWeek(Window window, Integer dayWeek);

}
