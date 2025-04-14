package mx.edu.utez.sacit.service;

import mx.edu.utez.sacit.model.Appointments;
import mx.edu.utez.sacit.model.Procedures;
import mx.edu.utez.sacit.model.Window;
import mx.edu.utez.sacit.repository.AppointmentRepository;
import mx.edu.utez.sacit.repository.WindowRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AvailabilityService {
    private final WindowRepository windowRepository;
    private final AppointmentRepository appointmentRepository;

    public AvailabilityService(WindowRepository windowRepository, AppointmentRepository appointmentRepository) {
        this.windowRepository = windowRepository;
        this.appointmentRepository = appointmentRepository;
    }


    public Window findAvailableWindow(LocalDate date, LocalTime requestedTime, Procedures procedure) {
        int totalDuration = procedure.getStimatedTime() + 15;
        LocalTime requestedEndTime = requestedTime.plusMinutes(totalDuration);

        List<Window> activeWindows = windowRepository.findByStatus("Activa");

        List<Window> availableWindows = activeWindows.stream()
                .filter(window -> isWindowAvailable(window, date, requestedTime, requestedEndTime))
                .toList();
        if (availableWindows.isEmpty()) {
            return null;
        }
        SecureRandom secureRandom = new SecureRandom();
        return availableWindows.get(secureRandom.nextInt(availableWindows.size()));
    }

    private boolean isWindowAvailable(Window window, LocalDate date,
                                      LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(window.getStartTime()) ||
                endTime.isAfter(window.getEndTime())) {
            return false;
        }

        List<Appointments> existingAppointments = appointmentRepository
                .findByDateAndWindowAndStatusNot(date, window, "CANCELLED");

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
