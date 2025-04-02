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
@Table(name = "procedures")
public class Procedures {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column( updatable = false, nullable = false,unique = true)
    private UUID uuid;

    private String name;
    private String description;
    private Double cost;

    @Column(name = "stimated_time")
    private Integer stimatedTime;

    @Column(name = "creation_date")
    private String creationDate;

    private String status;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private UserModel creator;

    @OneToMany(mappedBy = "procedure")
    private Set<RequieredDocuments> requieredDocuments = new HashSet<>();

    @OneToMany(mappedBy = "procedure")
    private Set<Appointments> appointments = new HashSet<>();

}
