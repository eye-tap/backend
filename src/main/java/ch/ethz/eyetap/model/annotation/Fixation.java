package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fixation")
public class Fixation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "x", nullable = false)
    private Long x;

    @Column(name = "y", nullable = false)
    private Long y;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reading_session_id", nullable = false)
    private ReadingSession readingSession;

}