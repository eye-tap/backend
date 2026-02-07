package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

/*

Association between user and annotation group

 */
@Getter
@Setter
@Entity
@Table(name = "annotation_session")
public class AnnotationSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Annotator annotator;

    @OneToMany(mappedBy = "annotationSession", orphanRemoval = true)
    private Set<Annotation> annotations = new LinkedHashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "reading_session_id", nullable = false)
    private ReadingSession readingSession;

}