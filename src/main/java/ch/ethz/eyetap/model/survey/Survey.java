package ch.ethz.eyetap.model.survey;

import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "survey")
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToMany
    @JoinTable(name = "survey_users",
            joinColumns = @JoinColumn(name = "survey_id"),
            inverseJoinColumns = @JoinColumn(name = "users_id"))
    private Set<User> users = new LinkedHashSet<>();

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "survey", orphanRemoval = true)
    private Set<AnnotationSession> annotationSessions = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(name = "survey_admin",
            joinColumns = @JoinColumn(name = "survey_id"),
            inverseJoinColumns = @JoinColumn(name = "admin_id"))
    private Set<User> admin = new LinkedHashSet<>();

}