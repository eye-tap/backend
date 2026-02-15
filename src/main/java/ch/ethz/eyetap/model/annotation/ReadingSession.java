package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
/*

Uniquely describes each text X reader X the corresponding fixations

 */
@Getter
@Setter
@Entity
@Table(name = "reading_session")
public class ReadingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany(mappedBy = "readingSession", orphanRemoval = true)
    private Set<Fixation> fixations = new LinkedHashSet<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "reader_id", nullable = false)
    private Reader reader;

    @ManyToOne(optional = false)
    @JoinColumn(name = "text_id", nullable = false)
    private Text text;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("fixations", fixations)
                .append("reader", reader)
                .append("text", text)
                .toString();
    }
}