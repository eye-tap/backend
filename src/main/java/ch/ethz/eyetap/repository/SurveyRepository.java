package ch.ethz.eyetap.repository;

import ch.ethz.eyetap.model.survey.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    @Query(value = """
            SELECT
                s.id AS survey_id,
                s.title,
                s.description,
                su.users_id AS user_id,
                a.id AS annotation_session_id,
                u.id AS annotator_id,
                rs.id AS reading_session_id,
                rs.reader_id AS reading_session_reader_id,
                t.id AS reading_session_text_id,
                t.title AS reading_session_text_title,
                COUNT(DISTINCT f.id) AS fixation_count,
                COUNT(DISTINCT ua.id) + COUNT(DISTINCT ma.id) AS annotated_count,
                a.last_edited,
                rs.uploaded_at,
                a.description AS annotation_session_description
            FROM survey s
            JOIN survey_users su ON s.id = su.survey_id
            JOIN app_user su_user ON su.users_id = su_user.id
            LEFT JOIN annotation_session a ON a.survey_id = s.id
            LEFT JOIN annotator an ON a.user_id = an.id
            LEFT JOIN app_user u ON an.user_id = u.id
            LEFT JOIN reading_session rs ON a.reading_session_id = rs.id
            LEFT JOIN text t ON rs.text_id = t.id
            LEFT JOIN fixation f ON f.reading_session_id = rs.id
            LEFT JOIN user_annotation ua ON ua.annotation_session_id = a.id
            LEFT JOIN annotation_session_machine_annotations asm ON asm.annotation_session_id = a.id
            LEFT JOIN machine_annotation ma ON ma.id = asm.machine_annotations_id
            WHERE s.id = :id
            GROUP BY
                s.id, s.title, s.description, su.users_id,
                a.id, u.id, rs.id, rs.reader_id, t.id, t.title,
                a.last_edited, rs.uploaded_at, a.description
            ORDER BY a.id
            """, nativeQuery = true)
    List<Object[]> findSurveyWithSessionsNative(@Param("id") Long id);


}
