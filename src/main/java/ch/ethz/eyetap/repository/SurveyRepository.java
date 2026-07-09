package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.survey.Survey;
import ch.ethz.eyetap.model.survey.SurveyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    Set<Survey> findAllBySurveyType(SurveyType surveyType);

}
