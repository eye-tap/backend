package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.AnnotationSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnotationSessionRepository extends JpaRepository<AnnotationSession, Long> {
}