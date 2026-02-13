package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.Annotator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface AnnotationSessionRepository extends JpaRepository<AnnotationSession, Long> {
    @Query("SELECT a FROM AnnotationSession a JOIN FETCH a.readingSession r WHERE a.annotator = :annotator")
    Set<AnnotationSession> findAllByAnnotator(@Param("annotator") Annotator annotator);

    // Returns only the IDs of annotation sessions for a given annotator
    @Query("SELECT a.id FROM AnnotationSession a WHERE a.annotator = :annotator")
    Set<Long> findAllIdsByAnnotator(@Param("annotator") Annotator annotator);

    @Query("""
                SELECT COUNT(f)
                FROM AnnotationSession a
                JOIN a.readingSession r
                JOIN r.fixations f
                WHERE a.id = :annotationSessionId
            """)
    long countTotalFixationsByAnnotationSessionId(@Param("annotationSessionId") Long id);

    @Query("""
                SELECT COUNT(an)
                FROM AnnotationSession a
                JOIN a.annotations an
                WHERE a.id = :annotationSessionId
                  AND (an.annotationType = ch.ethz.eyetap.model.annotation.AnnotationType.ANNOTATED
                       OR an.annotationType = ch.ethz.eyetap.model.annotation.AnnotationType.MACHINE_ANNOTATED)
            """)
    long countSetAnnotationsByAnnotationSessionId(@Param("annotationSessionId") Long annotationSessionId);

    @Query("""
                SELECT a.annotator.id
                FROM AnnotationSession a
                WHERE a.id = :annotationSessionId
            """)
    long annotatorByAnnotationSessionId(@Param("annotationSessionId") Long annotationSessionId);


    @Query("""
                SELECT a.readingSession.id
                FROM AnnotationSession a
                WHERE a.id = :annotationSessionId
            """)
    long readingSessionByAnnotationSessionId(@Param("annotationSessionId") Long annotationSessionId);
}