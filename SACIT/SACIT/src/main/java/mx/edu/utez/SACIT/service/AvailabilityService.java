package mx.edu.utez.SACIT.service;

import mx.edu.utez.SACIT.model.Appointments;
import mx.edu.utez.SACIT.model.Procedures;
import mx.edu.utez.SACIT.model.Window;
import mx.edu.utez.SACIT.repository.AppointmentRepository;
import mx.edu.utez.SACIT.repository.WindowRepository;
import mx.edu.utez.SACIT.utils.Utilities;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AvailabilityService {
    private final WindowRepository windowRepository;
    private final AppointmentRepository appointmentRepository;

    public AvailabilityService(WindowRepository windowRepository, AppointmentRepository appointmentRepository) {
        this.windowRepository = windowRepository;
        this.appointmentRepository = appointmentRepository;
    }


    public Window findAvailableWindow(LocalDate date, LocalTime requestedTime, Procedures procedure) {
        // Calcular tiempo total (estimado + buffer)
        int totalDuration = procedure.getStimatedTime() + 15;
        LocalTime requestedEndTime = requestedTime.plusMinutes(totalDuration);

        // Obtener ventanillas activas
        List<Window> activeWindows = windowRepository.findByStatus("Activa");

        List<Window> availableWindows = activeWindows.stream()
                .filter(window -> isWindowAvailable(window, date, requestedTime, requestedEndTime))
                .toList();
        if (availableWindows.isEmpty()) {
            return null; // No hay ventanillas disponibles
        }
        Random random = new Random();
        return  availableWindows.get(random.nextInt(availableWindows.size()));
    }

    private boolean isWindowAvailable(Window window, LocalDate date,
                                      LocalTime startTime, LocalTime endTime) {
        // Validar horario de trabajo de la ventanilla
        if (startTime.isBefore(window.getStartTime()) ||
                endTime.isAfter(window.getEndTime())) {
            return false;
        }

        // Obtener citas existentes para esa ventanilla y fecha
        List<Appointments> existingAppointments = appointmentRepository
                .findByDateAndWindowAndStatusNot(date, window, "CANCELLED");

        // Verificar traslapes
        return existingAppointments.stream().noneMatch(appointment ->
                isTimeOverlapping(
                        appointment.getStartTime(),
                        appointment.getEndTime(),
                        startTime,
                        endTime
                )
        );
    }

    private boolean isTimeOverlapping(LocalTime start1, LocalTime end1,
                                      LocalTime start2, LocalTime end2) {
        return !end1.isBefore(start2) && !end2.isBefore(start1);
    }

}
