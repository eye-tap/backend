package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    @Query("""
            SELECT rs FROM ReadingSession rs
            JOIN FETCH rs.fixations
            WHERE rs.id = :id
            """)
    ReadingSession findWithFixations(@Param("id") Long id);

    // Returns the reader ID for a given reading session
    @Query("SELECT rs.reader.id FROM ReadingSession rs WHERE rs.id = :id")
    Long findReaderIdByReadingSession(@Param("id") Long id);

    // Returns the text ID for a given reading session
    @Query("SELECT rs.text.id FROM ReadingSession rs WHERE rs.id = :id")
    Long findTextIdByReadingSession(@Param("id") Long id);

    // Returns the text title for a given reading session
    @Query("SELECT rs.text.title FROM ReadingSession rs WHERE rs.id = :id")
    String findTextTitleByReadingSession(@Param("id") Long id);

    @Query("""
                SELECT rs.uploadedAt
                FROM ReadingSession rs
                WHERE rs.id = :id
            """)
    LocalDateTime lastEditedByAnnotationSessionId(
            @Param("id") Long id
    );
}