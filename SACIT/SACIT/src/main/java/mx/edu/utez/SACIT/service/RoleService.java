package mx.edu.utez.SACIT.service;

import jakarta.transaction.Transactional;
import mx.edu.utez.SACIT.model.RoleModel;
import mx.edu.utez.SACIT.repository.RoleRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

public Optional<RoleModel> findById(Integer id) {
        return repository.findById(id);
}
    public RoleModel saveRole (RoleModel role){
        return repository.save(role);
    }

    public void delete(Integer id) {
        this.repository.deleteById(id);
    }
}
