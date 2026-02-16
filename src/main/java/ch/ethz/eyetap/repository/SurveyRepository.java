package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.survey.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    // todo: in annotated_counts also factor in machine annotations
    @Query(value = """
            SELECT
                s.id AS survey_id,
                s.title,
                s.description,
                ARRAY_AGG(DISTINCT su.users_id) AS user_ids,
                ARRAY_AGG(DISTINCT a.id) AS annotation_session_ids,
                ARRAY_AGG(DISTINCT u.id) AS annotator_user_ids,
                ARRAY_AGG(DISTINCT rs.id) AS reading_session_ids,
                ARRAY_AGG(DISTINCT rs.reader_id) AS reading_session_reader_ids,
                ARRAY_AGG(DISTINCT t.id) AS reading_session_text_ids,
                ARRAY_AGG(DISTINCT t.title) AS reading_session_text_titles,
                COUNT(DISTINCT a_annotations.id) AS annotation_counts,
                COUNT(DISTINCT f.id) - COUNT(DISTINCT a_annotations.id) AS annotated_counts,
                ARRAY_AGG(DISTINCT a.last_edited) AS annotation_session_last_edited,
                ARRAY_AGG(DISTINCT rs.uploaded_at) AS reading_session_uploaded_at,
                ARRAY_AGG(DISTINCT a.description) AS annotation_session_description
            FROM survey s
            JOIN survey_users su ON s.id = su.survey_id
            JOIN app_user su_user ON su.users_id = su_user.id
            LEFT JOIN annotation_session a ON a.survey_id = s.id
            LEFT JOIN annotation a_annotations ON a.id = a_annotations.annotation_session_id
            LEFT JOIN annotator an ON a.user_id = an.id
            LEFT JOIN app_user u ON an.user_id = u.id
            LEFT JOIN reading_session rs ON a.reading_session_id = rs.id
            LEFT JOIN text t ON rs.text_id = t.id
            LEFT JOIN fixation f ON f.reading_session_id = rs.id
            WHERE s.id = :id
            GROUP BY s.id, s.title, s.description
            """, nativeQuery = true)
    Object findSurveyWithSessionsNative(@Param("id") Long id);


}
