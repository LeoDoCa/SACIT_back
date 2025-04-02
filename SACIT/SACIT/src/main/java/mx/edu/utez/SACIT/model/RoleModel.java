package mx.edu.utez.SACIT.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class RoleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column( updatable = false, nullable = false,unique = true)
    private UUID uuid;

    private String role;

    @OneToMany
    private Set<UserModel> users = new HashSet<>();

    public RoleModel(String role) {
        this.role = role;
    }
}
