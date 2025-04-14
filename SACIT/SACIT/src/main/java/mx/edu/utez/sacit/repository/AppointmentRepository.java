package mx.edu.utez.sacit.repository;

import mx.edu.utez.sacit.model.Appointments;
import mx.edu.utez.sacit.model.UserModel;
import mx.edu.utez.sacit.model.Window;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointments, Integer> {
    Appointments findByUuid(UUID uuid);

    List<Appointments> findByDateAndWindowAndStatusNot(LocalDate date, Window window, String status);
    List<Appointments> findByWindow(Window window);
    List<Appointments> findByUserAndStatus(UserModel user, String status);
    List<Appointments> findByDate(LocalDate date);
}