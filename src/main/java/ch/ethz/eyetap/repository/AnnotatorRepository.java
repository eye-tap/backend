package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.Annotator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnotatorRepository extends JpaRepository<Annotator, Long> {

}
