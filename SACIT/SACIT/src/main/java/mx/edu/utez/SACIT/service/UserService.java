package mx.edu.utez.SACIT.service;

import jakarta.transaction.Transactional;
import mx.edu.utez.SACIT.model.PasswordResetToken;
import mx.edu.utez.SACIT.model.UserModel;
import mx.edu.utez.SACIT.repository.PasswordResetTokenRepository;
import mx.edu.utez.SACIT.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public void delete(Integer id){
        this.repository.deleteById(id);
    }

    public UserModel findByEmail(String email){
        return this.repository.findByEmail(email);
    }

    public void savePasswordResetToken(UserModel user, String token){
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(java.time.LocalDateTime.now().plusHours(1));
        passwordRepository.save(resetToken);
    }
}
