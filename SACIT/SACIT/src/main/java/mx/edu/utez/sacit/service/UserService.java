package mx.edu.utez.sacit.service;

import jakarta.transaction.Transactional;
import mx.edu.utez.sacit.model.PasswordResetToken;
import mx.edu.utez.sacit.model.UserModel;
import mx.edu.utez.sacit.model.Window;
import mx.edu.utez.sacit.repository.PasswordResetTokenRepository;
import mx.edu.utez.sacit.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
@Transactional
public class UserService {
    private final UserRepository repository;
    private final PasswordResetTokenRepository passwordRepository;

    public UserService(UserRepository repository, PasswordResetTokenRepository passwordRepository) {
        this.repository = repository;
        this.passwordRepository = passwordRepository;
    }

    public List<UserModel> getAll() {
        return this.repository.findAll(Sort.by("id").ascending());
    }

    public UserModel findByUuid(UUID uuid) {
        Optional<UserModel> optional = repository.findByUuid(uuid);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public UserModel findById(Integer id){
        Optional<UserModel> optional = repository.findById(id);
        if (optional.isPresent()){
            return optional.get();
        }
        return null;
    }
    public void save(UserModel user){
        this.repository.save(user);
    }

    public void delete(UUID uuid) {
        Optional<UserModel> optional = repository.findByUuid(uuid);
        if (optional.isPresent()) {
            this.repository.delete(optional.get());
        }
    }

    public UserModel findByEmail(String email){
        return this.repository.findByEmail(email);
    }

    public void savePasswordResetToken(UserModel user, String token){
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
        passwordRepository.save(resetToken);
    }

    public List<UserModel> getUsersByRoleName(String roleName) {
        return repository.findByRole_Role(roleName);
    }

    public UUID getAttendedWindowUuidByUserUuid(UUID userUuid) {
        Optional<UserModel> optionalUser = repository.findByUuid(userUuid);

        if (optionalUser.isPresent()) {
            UserModel user = optionalUser.get();

            if (user.getRole() == null || !"ROLE_WINDOW".equals(user.getRole().getRole())) {
                throw new IllegalStateException("El usuario no tiene permiso para realizar esta acción.");
            }

            Window attendedWindow = user.getAttendedWindow();

            if (attendedWindow != null) {
                return attendedWindow.getUuid();
            } else {
                throw new IllegalStateException("El usuario no está atendiendo ninguna ventana.");
            }
        } else {
            throw new IllegalArgumentException("Usuario no encontrado con el UUID proporcionado.");
        }
    }
}
