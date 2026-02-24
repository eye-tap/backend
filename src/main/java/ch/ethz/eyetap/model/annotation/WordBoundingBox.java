package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "word_bounding_box", indexes = {
        @Index(name = "idx_wordboundingbox", columnList = "foreign_id")
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WordBoundingBox {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Embedded
    private BoundingBox boundingBox;

    @ManyToOne(optional = false)
    @JoinColumn(name = "text_id", nullable = false)
    private Text text;

    @Column(name = "word", nullable = false)
    private String word;

    @Column(name = "foreign_id")
    private Long foreignId;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        WordBoundingBox that = (WordBoundingBox) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}