package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.MachineAnnotation;
import ch.ethz.eyetap.model.annotation.UserAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MachineAnnotationRepository extends JpaRepository<MachineAnnotation, Long> {
    @Query("""
                SELECT DISTINCT m.title
                FROM MachineAnnotation m
                WHERE m.readingSession.id = :readingSessionId
            """)
    Set<String> findAllMachineAnnotationTitle(@Param("readingSessionId") Long readingSessionId);

    @Query("""
                SELECT m
                FROM MachineAnnotation m
                WHERE m.title = :title
                  AND m.readingSession.id = :readingSessionId
            """)
    Set<MachineAnnotation> findByTitleAndReadingSession(
            @Param("title") String title,
            @Param("readingSessionId") Long readingSessionId
    );

    @Query("""
                SELECT m
                FROM MachineAnnotation m
                WHERE m.readingSession.id IN :readingSessionIds
            """)
    List<MachineAnnotation> findAllByReadingSessionIds(@Param("readingSessionIds") List<Long> readingSessionIds);

}