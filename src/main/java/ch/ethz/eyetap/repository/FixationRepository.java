package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.Fixation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface FixationRepository extends JpaRepository<Fixation, Long> {

    boolean existsByForeignId(Long foreignId);

    Fixation findByForeignId(Long foreignId);

    Collection<Fixation> findAllByIdIsIn(List<Long> list);
}