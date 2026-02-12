package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    @Query("""
            SELECT rs FROM ReadingSession rs
            JOIN FETCH rs.fixations
            WHERE rs.id = :id
            """)
    ReadingSession findWithFixations(@Param("id") Long id);

}