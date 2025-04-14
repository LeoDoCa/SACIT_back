package mx.edu.utez.sacit.repository;

import mx.edu.utez.sacit.model.UnloggedUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
@Repository
public interface UnloggedUsersRepository extends JpaRepository<UnloggedUsers,Integer> {
    Optional<UnloggedUsers> findByUuid(UUID uuid);
    Optional<UnloggedUsers> findByEmail(String email);
}
