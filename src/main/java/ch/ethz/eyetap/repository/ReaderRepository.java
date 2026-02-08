package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.Reader;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReaderRepository extends JpaRepository<Reader, Long> {

    boolean existsByForeignId(Long foreignId);

    Reader findByForeignId(Long foreignId);
}