package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.Appointments;
import mx.edu.utez.SACIT.model.Window;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.invoke.CallSite;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointments, Integer> {
    Appointments findByUuid(UUID uuid);

    List<Appointments> findByDateAndWindowAndStatusNot(LocalDate date, Window window, String status);
}