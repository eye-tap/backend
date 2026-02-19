package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@Table(name = "machine_annotation")
@NoArgsConstructor
@AllArgsConstructor
public class MachineAnnotation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToOne
    @JoinColumn(name = "fixation_id")
    private Fixation fixation;

    @ManyToOne
    @JoinColumn(name = "character_bounding_box_id")
    private CharacterBoundingBox characterBoundingBox;

    @ManyToOne
    @JoinColumn(name = "word_bounding_box_id")
    private WordBoundingBox wordBoundingBox;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "reading_session_id")
    private ReadingSession readingSession;

    @ManyToMany(mappedBy = "machineAnnotations")
    private Set<AnnotationSession> annotationSessions = new LinkedHashSet<>();

    @Column(name = "d_geom_weight")
    private Double dGeomWeight;

    @Column(name = "p_share_weight")
    private Double pShareWeight;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        MachineAnnotation that = (MachineAnnotation) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}