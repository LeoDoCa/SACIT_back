package mx.edu.utez.SACIT.repository;

import mx.edu.utez.SACIT.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel,Integer> {
    UserModel findByEmail(String email);
}
