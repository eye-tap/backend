package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.Text;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TextRepository extends JpaRepository<Text, Long> {
    boolean existsByTitle(String title);

    Optional<Text> findByForeignIdAndLanguage(Long foreignId, String langauge);
}