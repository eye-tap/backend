package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
    /**
     * Finds an annotation by its ID that belongs to a specific annotation session.
     */
    @Query("""
        SELECT a
        FROM Annotation a
        WHERE a.id = :annotationId
          AND a.annotationSession.id = :sessionId
    """)
    Optional<Annotation> findByIdAndSessionId(@Param("annotationId") Long annotationId,
                                              @Param("sessionId") Long sessionId);
}