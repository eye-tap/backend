package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.Fixation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FixationRepository extends JpaRepository<Fixation, Long> {
}