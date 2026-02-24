package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fixation", indexes = {
        @Index(name = "idx_fixation", columnList = "reading_session_id")
})
public class Fixation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "x", nullable = false)
    private Double x;

    @Column(name = "y", nullable = false)
    private Double y;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reading_session_id", nullable = false)
    private ReadingSession readingSession;

    @Column(name = "foreign_id", nullable = false)
    private Long foreignId;

    @Column(name = "disagreement")
    private Double disagreement = 0.0;

}