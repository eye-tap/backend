package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.CharacterBoundingBox;
import ch.ethz.eyetap.model.annotation.WordBoundingBox;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordBoundingBoxRepository extends JpaRepository<WordBoundingBox, Long> {
}
