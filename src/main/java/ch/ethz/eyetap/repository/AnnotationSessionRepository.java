package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.dto.ShallowAnnotationSessionDto;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.Annotator;
import ch.ethz.eyetap.model.survey.ShallowAnnotationSessionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface AnnotationSessionRepository extends JpaRepository<AnnotationSession, Long> {
    @Query("SELECT a FROM AnnotationSession a JOIN FETCH a.readingSession r WHERE a.annotator = :annotator")
    Set<AnnotationSession> findAllByAnnotator(@Param("annotator") Annotator annotator);

    @Query("""
            SELECT a.id AS id,
                   a.annotator.id AS annotatorId,
                   a.readingSession.id AS readingSessionId,
                   a.readingSession.reader.id AS readingSessionReaderId,
                   a.readingSession.text.id AS readingSessionTextId,
                   a.readingSession.text.title AS readingSessionTextTitle
            FROM AnnotationSession a
            WHERE a.id IN :ids
            """)
    List<ShallowAnnotationSessionProjection> findShallowByIds(@Param("ids") Set<Long> ids);


}