package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Fixation fixation = (Fixation) o;

        return new EqualsBuilder().append(id, fixation.id).append(x, fixation.x).append(y, fixation.y).append(readingSession, fixation.readingSession).append(foreignId, fixation.foreignId).append(disagreement, fixation.disagreement).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(x).append(y).append(readingSession).append(foreignId).append(disagreement).toHashCode();
    }
}