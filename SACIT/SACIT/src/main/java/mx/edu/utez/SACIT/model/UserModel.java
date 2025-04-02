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
@Table(name = "users")
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column( updatable = false, nullable = false,unique = true)
    private UUID uuid;
    private String name;
    private String lastName;
    private String email;
    private String password;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleModel role;

    @OneToMany(mappedBy = "creator")
    private Set<Procedures> createdProcedures = new HashSet<>();

    @OneToMany(mappedBy = "attendant")
    private Set<Window> attendedWindows = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Appointments> appointments = new HashSet<>();

    public UserModel(UUID uuid, String name, String lastName, String email, String password, RoleModel role) {
        this.uuid = uuid;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public UserModel(String name, String lastName, String email, String password, RoleModel role) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}
