package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserModel,Integer> {
    UserModel findByEmail(String email);

    Optional<UserModel> findByUuid(UUID uuid);

    List<UserModel> findByRole_Role(String role);
}
