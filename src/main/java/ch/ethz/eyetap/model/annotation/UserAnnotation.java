package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@Table(name = "user_annotation")
@NoArgsConstructor
@AllArgsConstructor
public class UserAnnotation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fixation_id")
    private Fixation fixation;

    @ManyToOne
    @JoinColumn(name = "annotation_session_id")
    private AnnotationSession annotationSession;

    @ManyToOne
    @JoinColumn(name = "character_bounding_box_id")
    private CharacterBoundingBox characterBoundingBox;

    @ManyToOne
    @JoinColumn(name = "word_bounding_box_id")
    private WordBoundingBox wordBoundingBox;

}