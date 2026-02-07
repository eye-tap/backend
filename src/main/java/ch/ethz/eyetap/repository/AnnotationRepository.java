package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
}