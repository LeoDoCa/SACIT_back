package mx.edu.utez.SACIT.service;

import mx.edu.utez.SACIT.dto.WindowScheduleDTO;
import mx.edu.utez.SACIT.model.Window;
import mx.edu.utez.SACIT.model.WindowSchedule;
import mx.edu.utez.SACIT.repository.WindowRepository;
import mx.edu.utez.SACIT.repository.WindowScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class WindowsScheduleService {
    private final WindowScheduleRepository windowScheduleRepository;
    private final WindowRepository windowRepository;

    public WindowsScheduleService(WindowScheduleRepository windowScheduleRepository, WindowRepository windowRepository) {
        this.windowScheduleRepository = windowScheduleRepository;
        this.windowRepository = windowRepository;
    }

    public List<WindowSchedule> getAll() {
        return this.windowScheduleRepository.findAll();
    }


    public Optional<WindowSchedule> findByUuuid(UUID uuid){
       return windowScheduleRepository.findByUuid(uuid);
    }

    public WindowSchedule save(WindowScheduleDTO dto) {
        if (dto.getDayWeek() < 1 || dto.getDayWeek() > 5) {
            throw new IllegalArgumentException("Day of the week must be between 1 and 5");
        }

        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        LocalTime minTime = LocalTime.of(9, 0);
        LocalTime maxTime = LocalTime.of(15, 0);
        if (dto.getStartTime().isBefore(minTime) || dto.getEndTime().isAfter(maxTime)) {
            throw new IllegalArgumentException("Start time must be after 9:00 and end time must be before 15:00");
        }

        Window window = windowRepository.findByUuid(dto.getWindowUuid())
                .orElseThrow(() -> new IllegalArgumentException("Window not found"));

        WindowSchedule windowSchedule = new WindowSchedule();
        windowSchedule.setDayWeek(dto.getDayWeek());
        windowSchedule.setStartTime(dto.getStartTime());
        windowSchedule.setEndTime(dto.getEndTime());
        windowSchedule.setWindow(window);

        return windowScheduleRepository.save(windowSchedule);
    }


    public WindowSchedule saved(WindowScheduleDTO dto){
        if (dto.getDayWeek()<1 || dto.getDayWeek()>5){
            throw new IllegalArgumentException("Day of the week must be between 1 and 5");

        }
        if (dto.getStartTime().isAfter(dto.getEndTime())){
            throw new IllegalArgumentException("Start time must be before end time");
        }

        LocalTime minTime = LocalTime.of(9,0);
        LocalTime maxTime = LocalTime.of(15,0);
        if (dto.getStartTime().isBefore(minTime) || dto.getEndTime().isAfter(maxTime)){
            throw new IllegalArgumentException("Start time must be after 9:00 and end time must be before 15:00");
        }

        Window window = windowRepository.findByUuid(dto.getWindowUuid())
                .orElseThrow(() -> new IllegalArgumentException("Window not found"));

        WindowSchedule windowSchedule = new WindowSchedule();
        windowSchedule.setDayWeek(dto.getDayWeek());
        windowSchedule.setStartTime(dto.getStartTime());
        windowSchedule.setEndTime(dto.getEndTime());
        windowSchedule.setWindow(window);
        return windowScheduleRepository.save(windowSchedule);
    }

    public void delete(UUID uuid){
        Optional<WindowSchedule> optional = windowScheduleRepository.findByUuid(uuid);
        if (optional.isPresent()){
            windowScheduleRepository.delete(optional.get());
        }else {
            throw new IllegalArgumentException("Window schedule not found");
        }
    }
}
