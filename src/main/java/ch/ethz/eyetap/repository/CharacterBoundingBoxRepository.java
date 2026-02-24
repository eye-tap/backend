package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.CharacterBoundingBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface CharacterBoundingBoxRepository extends JpaRepository<CharacterBoundingBox, Long> {
    Optional<CharacterBoundingBox> findCharacterBoundingBoxByForeignId(Long id);

    Collection<CharacterBoundingBox> findAllByForeignIdIn(Set<Long> allCharIds);
}
