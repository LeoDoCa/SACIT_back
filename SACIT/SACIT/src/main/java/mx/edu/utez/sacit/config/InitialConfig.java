package mx.edu.utez.sacit.config;


import lombok.RequiredArgsConstructor;
import mx.edu.utez.sacit.model.RoleModel;
import mx.edu.utez.sacit.model.UserModel;
import mx.edu.utez.sacit.repository.RoleRepository;
import mx.edu.utez.sacit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class InitialConfig implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${user.password}")
    private String userPassword;

    @Value("${window.password}")
    private String windowPassword;

    @Override
    public void run(String... args) throws Exception {
        RoleModel adminRole = getOrCreateRole("ROLE_ADMIN");
        RoleModel userRole = getOrCreateRole("ROLE_USER");
        RoleModel windowRole = getOrCreateRole("ROLE_WINDOW");

        createUserIfNotExists("sacit3mail@gmail.com", "Leonardo", "Dorantes", adminPassword, adminRole);
        createUserIfNotExists("20223tn049@utez.edu.mx", "Daniel", "Castañeda", userPassword, userRole);
        createUserIfNotExists("20223tn055@utez.edu.mx", "Moises", "Gonzalez", windowPassword, windowRole);
    }

    private RoleModel getOrCreateRole(String roleName) {
        Optional<RoleModel> roleOpt = roleRepository.findByRole(roleName);

        if (roleOpt.isPresent()) {
            return roleOpt.get();
        } else {
            RoleModel newRole = new RoleModel();
            newRole.setRole(roleName);
            newRole.setUuid(UUID.randomUUID());
            return roleRepository.save(newRole);
        }
    }

    private void createUserIfNotExists(String email, String name, String lastName, String password, RoleModel role) {
        Optional<UserModel> userOpt = Optional.ofNullable(userRepository.findByEmail(email));

        if (userOpt.isEmpty()) {
            UserModel user = new UserModel();
            user.setUuid(UUID.randomUUID());
            user.setName(name);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            userRepository.save(user);
        }
    }
}