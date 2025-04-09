package mx.edu.utez.SACIT.config;


import lombok.RequiredArgsConstructor;
import mx.edu.utez.SACIT.model.RoleModel;
import mx.edu.utez.SACIT.model.UserModel;
import mx.edu.utez.SACIT.repository.RoleRepository;
import mx.edu.utez.SACIT.repository.UserRepository;
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

    @Override
    public void run(String... args) throws Exception {
        RoleModel adminRole = getOrCreateRole("ROLE_ADMIN");
        RoleModel userRole = getOrCreateRole("ROLE_USER");
        RoleModel windowRole = getOrCreateRole("ROLE_WINDOW");

        Optional<UserModel> adminUserOpt = Optional.ofNullable(userRepository.findByEmail("admin@gmail.com"));

        if (adminUserOpt.isEmpty()) {
            UserModel user = new UserModel();
            user.setUuid(UUID.randomUUID());
            user.setName("Leonardo");
            user.setLastName("Dorantes");
            user.setEmail("admin@gmail.com");
            user.setPassword(passwordEncoder.encode("$4dmin_123!"));
            user.setRole(adminRole);
            userRepository.save(user);
        }

        UserModel user = new UserModel();
        user.setUuid(UUID.randomUUID());
        user.setName("Daniel");
        user.setLastName("Castañeda");
        user.setEmail("20223tn049@utez.edu.mx");
        user.setPassword(passwordEncoder.encode("Leo1234$"));
        user.setRole(userRole);
        userRepository.save(user);

        UserModel userWindow = new UserModel();
        userWindow.setUuid(UUID.randomUUID());
        userWindow.setName("Moises");
        userWindow.setLastName("Gonzalez");
        userWindow.setEmail("20223tn055@utez.edu.mx");
        userWindow.setPassword(passwordEncoder.encode("Moy1234$"));
        userWindow.setRole(windowRole);
        userRepository.save(userWindow);
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
}