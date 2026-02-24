package ch.ethz.eyetap.model.annotation;

import ch.ethz.eyetap.model.survey.Survey;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/*

Association between user and annotation group

 */
@Getter
@Setter
@Entity
@Table(name = "annotation_session", indexes = {
        @Index(name = "idx_annotationsession", columnList = "survey_id")
})
public class AnnotationSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Annotator annotator;

    @OneToMany(mappedBy = "annotationSession")
    private Set<UserAnnotation> userAnnotations;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reading_session_id", nullable = false)
    private ReadingSession readingSession;

    @ManyToOne
    @JoinColumn(name = "survey_id")
    private Survey survey;

    @Column(name = "last_edited", nullable = false)
    private LocalDateTime lastEdited;

    @ManyToMany
    @JoinTable(name = "annotation_session_machine_annotations",
            joinColumns = @JoinColumn(name = "annotationSession_id"),
            inverseJoinColumns = @JoinColumn(name = "machineAnnotations_id"))
    private Set<MachineAnnotation> machineAnnotations = new LinkedHashSet<>();

    @Column(name = "description")
    private String description;

    @Column(name = "uses_partial_machine_annotation", nullable = false)
    private Boolean usesPartialMachineAnnotation = false;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("annotator", annotator)
                .append("readingSession", readingSession)
                .append("survey", survey)
                .toString();
    }
}