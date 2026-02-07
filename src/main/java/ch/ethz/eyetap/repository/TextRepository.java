package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.annotation.Text;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TextRepository extends JpaRepository<Text, Long> {
}