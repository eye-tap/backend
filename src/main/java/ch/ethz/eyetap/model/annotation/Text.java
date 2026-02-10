package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    // TODO: Add wordCount, totalPoints, annotatedPoints?
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

    @Column(name = "foreign_id")
    private Long foreignId;

}
