package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "text")
public class Text {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "title", unique = true)
    private String title;

    @OneToMany(mappedBy = "text", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReadingSession> readingSessions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "text", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WordBoundingBox> wordBoundingBoxes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "text", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CharacterBoundingBox> characterBoundingBoxes = new LinkedHashSet<>();

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "background_image")
    private byte[] backgroundImage;

    @Column(name = "foreign_id", unique = true)
    private Long foreignId;

    @Column(name = "language")
    private String language;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("title", title)
                .append("wordBoundingBoxes", wordBoundingBoxes)
                .append("characterBoundingBoxes", characterBoundingBoxes)
                .append("backgroundImage", backgroundImage)
                .append("foreignId", foreignId)
                .toString();
    }
}
