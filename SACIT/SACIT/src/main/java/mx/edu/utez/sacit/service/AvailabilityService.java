package mx.edu.utez.sacit.service;

import mx.edu.utez.sacit.model.Appointments;
import mx.edu.utez.sacit.model.Procedures;
import mx.edu.utez.sacit.model.Window;
import mx.edu.utez.sacit.repository.AppointmentRepository;
import mx.edu.utez.sacit.repository.ProcedureRepository;
import mx.edu.utez.sacit.repository.WindowRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AvailabilityService {
    private final WindowRepository windowRepository;
    private final AppointmentRepository appointmentRepository;
    private final ProcedureRepository procedureRepository;

    public AvailabilityService(WindowRepository windowRepository, AppointmentRepository appointmentRepository, ProcedureRepository procedureRepository) {
        this.windowRepository = windowRepository;
        this.appointmentRepository = appointmentRepository;
        this.procedureRepository = procedureRepository;
    }


    public Window findAvailableWindow(LocalDate date, LocalTime requestedTime, Procedures procedure) {
        int totalDuration = procedure.getStimatedTime();
        LocalTime requestedEndTime = requestedTime.plusMinutes(totalDuration);

        List<Window> activeWindows = windowRepository.findByStatus("Activa");

        List<Window> availableWindows = activeWindows.stream()
                .filter(window -> isWindowAvailable(window, date, requestedTime, requestedEndTime))
                .collect(Collectors.toList());

        if (availableWindows.isEmpty()) {
            throw new RuntimeException("No available windows");
        }

        return availableWindows.get(0);
    }

    private boolean isWindowAvailable(Window window, LocalDate date,
                                      LocalTime startTime, LocalTime endTime) {
        if (startTime.isBefore(window.getStartTime()) || endTime.isAfter(window.getEndTime())) {
            return false;
        }

        List<Appointments> existingAppointments = appointmentRepository
                .findByDateAndWindowAndStatusNot(date, window, "Finalizada");

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
        return !(end1.compareTo(start2) <= 0 || end2.compareTo(start1) <= 0);
    }

    public Map<String, Object> getAvailableTimes(LocalDate date, UUID procedureUuid) {
        LocalTime businessStart = LocalTime.of(9, 0);
        LocalTime businessEnd = LocalTime.of(15, 0);

        Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(procedureUuid);
        if (optionalProcedure.isEmpty()) {
            throw new IllegalArgumentException("Procedure not found");
        }

        Procedures procedure = optionalProcedure.get();
        int totalDuration = procedure.getStimatedTime();

        List<Window> activeWindows = windowRepository.findByStatus("Activa");
        if (activeWindows.isEmpty()) {
            return createEmptyResponse();
        }

        List<Appointments> existingAppointments = appointmentRepository
                .findByDateAndStatusNot(date, "Finalizada");

        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> availabilityData = new ArrayList<>();

        for (Window window : activeWindows) {
            LocalTime currentTime = window.getStartTime().isBefore(businessStart) ?
                    businessStart : window.getStartTime();

            LocalTime windowEndTime = window.getEndTime().isAfter(businessEnd) ?
                    businessEnd : window.getEndTime();

            List<LocalTime> availableSlots = new ArrayList<>();

            while (currentTime.plusMinutes(totalDuration).isBefore(windowEndTime) ||
                    currentTime.plusMinutes(totalDuration).equals(windowEndTime)) {

                LocalTime potentialEndTime = currentTime.plusMinutes(totalDuration);

                boolean slotAvailable = true;

                for (Appointments appointment : existingAppointments) {
                    LocalTime appointmentEndWithBuffer = appointment.getEndTime().plusMinutes(15);

                    if (appointment.getWindow().getId().equals(window.getId()) &&
                            isTimeOverlapping(appointment.getStartTime(),
                                    appointment.getEndTime().plusMinutes(15),
                                    currentTime,
                                    potentialEndTime)) {
                        slotAvailable = false;
                        break;
                    }
                }

                if (slotAvailable) {
                    availableSlots.add(currentTime);
                }

                currentTime = currentTime.plusMinutes(15);
            }

            if (!availableSlots.isEmpty()) {
                Map<String, Object> windowData = new HashMap<>();
                windowData.put("windowId", window.getId());
                windowData.put("windowUuid", window.getUuid());
                windowData.put("windowNumber", window.getWindowNumber());
                windowData.put("availableTimes", availableSlots.stream()
                        .map(LocalTime::toString)
                        .collect(Collectors.toList()));

                availabilityData.add(windowData);
            }
        }

        response.put("date", date.toString());
        response.put("procedureName", procedure.getName());
        response.put("procedureId", procedure.getId());
        response.put("procedureUuid", procedure.getUuid());
        response.put("estimatedDuration", procedure.getStimatedTime());
        response.put("availability", availabilityData);

        return response;
    }

    private Map<String, Object> createEmptyResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("availability", new ArrayList<>());
        return response;
    }

    public List<String> getAllAvailableTimes(LocalDate date, UUID procedureUuid) {
        LocalTime businessStart = LocalTime.of(9, 0);
        LocalTime businessEnd = LocalTime.of(15, 0);

        Optional<Procedures> optionalProcedure = procedureRepository.findByUuid(procedureUuid);
        if (optionalProcedure.isEmpty()) {
            throw new IllegalArgumentException("Procedure not found");
        }

        Procedures procedure = optionalProcedure.get();
        int totalDuration = procedure.getStimatedTime();

        List<Window> activeWindows = windowRepository.findByStatus("Activa");
        if (activeWindows.isEmpty()) {
            return Collections.emptyList();
        }

        List<Appointments> existingAppointments = appointmentRepository
                .findByDateAndStatusNot(date, "Finalizada");

        List<LocalTime> allAvailableSlots = new ArrayList<>();

        for (Window window : activeWindows) {
            LocalTime currentTime = window.getStartTime().isBefore(businessStart) ?
                    businessStart : window.getStartTime();

            LocalTime windowEndTime = window.getEndTime().isAfter(businessEnd) ?
                    businessEnd : window.getEndTime();

            while (currentTime.plusMinutes(totalDuration).isBefore(windowEndTime) ||
                    currentTime.plusMinutes(totalDuration).equals(windowEndTime)) {

                LocalTime potentialEndTime = currentTime.plusMinutes(totalDuration);

                boolean slotAvailable = true;

                for (Appointments appointment : existingAppointments) {
                    if (appointment.getWindow().getId().equals(window.getId()) &&
                            isTimeOverlapping(
                                    appointment.getStartTime(),
                                    appointment.getEndTime().plusMinutes(15),
                                    currentTime,
                                    potentialEndTime)) {
                        slotAvailable = false;
                        break;
                    }
                }

                if (slotAvailable) {
                    allAvailableSlots.add(currentTime);
                }

                currentTime = currentTime.plusMinutes(15);
            }
        }

        return allAvailableSlots.stream()
                .distinct()
                .sorted()
                .map(time -> time.format(DateTimeFormatter.ofPattern("HH:mm")))
                .collect(Collectors.toList());
    }

}
