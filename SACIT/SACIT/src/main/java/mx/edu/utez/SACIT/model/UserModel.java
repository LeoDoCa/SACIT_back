package mx.edu.utez.SACIT.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
        import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.sql.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    private Date creationDate;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleModel role;

    @JsonIgnore
    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private Set<Procedures> createdProcedures = new HashSet<>();

    @JsonIgnore
    @OneToOne(mappedBy = "attendant", cascade = CascadeType.ALL)
    private Window attendedWindow;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    @JsonIgnore
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

    @PrePersist
    public void generateUUID() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (creationDate == null) {
            creationDate = new Date(System.currentTimeMillis());
        }
    }

}
