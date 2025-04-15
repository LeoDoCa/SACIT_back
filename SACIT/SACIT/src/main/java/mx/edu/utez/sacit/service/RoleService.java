package mx.edu.utez.sacit.service;

import jakarta.transaction.Transactional;
import mx.edu.utez.sacit.model.RoleModel;
import mx.edu.utez.sacit.repository.RoleRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
@Transactional
public class RoleService {
    private final RoleRepository repository;

    public RoleService(RoleRepository repository) {
        this.repository = repository;
    }
    public List<RoleModel> getAll() {
        return this.repository.findAll(Sort.by("id").descending());
    }

    public Optional<RoleModel> findByUuid(UUID uuid) {
        return repository.findByUuid(uuid);
    }
    public RoleModel saveRole (RoleModel role){
        return repository.save(role);
    }

    public void delete(UUID uuid) {
        Optional<RoleModel> optional = repository.findByUuid(uuid);
        if (optional.isPresent()) {
            this.repository.delete(optional.get());
        }
    }
}
