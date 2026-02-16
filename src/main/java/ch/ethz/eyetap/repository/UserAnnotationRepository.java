package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.UserAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAnnotationRepository extends JpaRepository<UserAnnotation, Long> {
    /**
     * Finds an annotation by its ID that belongs to a specific annotation session.
     */
    @Query("""
        SELECT a
        FROM UserAnnotation a
        WHERE a.fixation.id = :fixationId
          AND a.annotationSession.id = :sessionId
    """)
    Optional<UserAnnotation> findByFixationIdAndSessionId(@Param("fixationId") Long fixationId,
                                                          @Param("sessionId") Long sessionId);
}