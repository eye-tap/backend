package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
}