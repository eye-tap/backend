package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.Fixation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface FixationRepository extends JpaRepository<Fixation, Long> {

    boolean existsByForeignId(Long foreignId);

    Fixation findByForeignId(Long foreignId);

    Collection<Fixation> findAllByIdIsIn(List<Long> list);

    @Query("""
                SELECT f.readingSession.id, COUNT(f)
                FROM Fixation f
                WHERE f.readingSession.id IN :readingSessionIds
                GROUP BY f.readingSession.id
            """)
    List<Object[]> findFixationCounts(@Param("readingSessionIds") List<Long> readingSessionIds);
}