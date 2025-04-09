package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.Appointments;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointments, Integer> {
    Appointments findByUuid(UUID uuid);
}