package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "character_bounding_box")
@Builder
@NonNull
@AllArgsConstructor
@NoArgsConstructor
public class CharacterBoundingBox {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Embedded
    private BoundingBox boundingBox;

    @ManyToOne(optional = false)
    @JoinColumn(name = "text_id", nullable = false)
    private Text text;

    @Column(name = "character", nullable = false)
    private String character;

    @Column(name = "foreign_id", nullable = false)
    private Long foreignId;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CharacterBoundingBox that = (CharacterBoundingBox) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}