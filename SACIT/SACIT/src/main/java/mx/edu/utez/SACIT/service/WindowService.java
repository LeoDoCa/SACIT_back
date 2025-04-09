package mx.edu.utez.SACIT.service;

import jakarta.transaction.Transactional;

import mx.edu.utez.SACIT.dto.WindowDTO;
import mx.edu.utez.SACIT.model.UserModel;
import mx.edu.utez.SACIT.model.Window;
import mx.edu.utez.SACIT.repository.UserRepository;
import mx.edu.utez.SACIT.repository.WindowRepository;


import org.springframework.context.annotation.Primary;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;



@Service
@Primary
@Transactional
public class WindowService {
    private final WindowRepository repository;
    private final UserRepository userRepository;

    public WindowService(WindowRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public List<Window> getAll() {
        return this.repository.findAll();
    }

    public Optional <Window> findByUuid(UUID uuid){
        return repository.findByUuid(uuid);
    }

    public void save(WindowDTO dto) {
        UserModel attendant = userRepository.findByUuid(dto.getAttendantUuid())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (attendant.getRole().getId() == 3 || "ROLE_WINDOW".equals(attendant.getRole().getRole())) {
            Integer nextWindowNumber = repository.findMaxWindowNumber().orElse(0) + 1;
            Window window = new Window();
            window.setStatus(dto.getStatus());
            window.setAttendant(attendant);
            window.setWindowNumber(nextWindowNumber);
            repository.save(window);
        } else {
            throw new RuntimeException("El usuario no tiene el rol adecuado para ser atendente.");
        }
    }








        public void delete (UUID uuid) {
        Optional<Window> optional = repository.findByUuid(uuid);
        if (optional.isPresent()) {
            repository.delete(optional.get());
        } else {
            throw new IllegalArgumentException("Window not found");
        }
    }

    public void update(Window dto) {
        UserModel attendant = userRepository.findByUuid(dto.getAttendant().getUuid())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (attendant.getRole().getId() == 3 || "ROLE_WINDOW".equals(attendant.getRole().getRole())) {


            Window window = new Window();
            window.setStatus(dto.getStatus());
            window.setWindowNumber(dto.getWindowNumber());
            window.setAttendant(attendant);


            repository.save(window);
        } else {
            throw new RuntimeException("El usuario no tiene el rol adecuado para ser atendente.");
        }
    }}
