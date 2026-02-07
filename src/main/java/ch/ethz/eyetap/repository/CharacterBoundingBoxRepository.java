package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.CharacterBoundingBox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterBoundingBoxRepository extends JpaRepository<CharacterBoundingBox, Long> {
}
