package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.survey.Survey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {
}
