package mx.edu.utez.SACIT.service;

import jakarta.transaction.Transactional;
import mx.edu.utez.SACIT.model.RoleModel;
import mx.edu.utez.SACIT.model.Window;
import mx.edu.utez.SACIT.repository.WindowRepository;
import mx.edu.utez.SACIT.utils.Utilities;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
@Transactional
public class WindowService {
    private final WindowRepository repository;

    public WindowService(WindowRepository repository) {
        this.repository = repository;
    }

    public List<Window> getAll() {
        return this.repository.findAll();
    }

    public Optional <Window> findByUuid(UUID uuid){
        return repository.findByUuid(uuid);
    }

    public void save(Window window){

            RoleModel attendet = window.getAttendant().getRole();
            if (attendet.getId() == 3 || attendet.getRole().equals("ROLE_WINDOW")){
                repository.save(window);
            }


    }

    public void delete(UUID uuid) {
        Optional<Window> optional = repository.findByUuid(uuid);
        if (optional.isPresent()) {
            this.repository.delete(optional.get());
        }
    }

}
