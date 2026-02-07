package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
}