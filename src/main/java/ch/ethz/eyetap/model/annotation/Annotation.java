package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "annotation")
public class Annotation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "annotation_type", nullable = false)
    private AnnotationType annotationType;

    @ManyToOne
    @JoinColumn(name = "fixation_id")
    private Fixation fixation;

    @ManyToOne
    @JoinColumn(name = "annotation_session_id")
    private AnnotationSession annotationSession;

    @ManyToOne(optional = false)
    @JoinColumn(name = "character_bounding_box_id", nullable = false)
    private CharacterBoundingBox characterBoundingBox;

    @ManyToOne
    @JoinColumn(name = "word_bounding_box_id")
    private WordBoundingBox wordBoundingBox;

}
