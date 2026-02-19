package ch.ethz.eyetap.service.export;

import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.UserAnnotation;
import ch.ethz.eyetap.model.survey.Survey;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExportService {

    @SneakyThrows
    public byte[] exportSurvey(Survey survey) {

        StringWriter stringWriter = new StringWriter();
        stringWriter.write('\uFEFF');

        StatefulBeanToCsv<ExportAnnotationRow> csv =
                new StatefulBeanToCsvBuilder<ExportAnnotationRow>(stringWriter)
                        .withApplyQuotesToAll(false)
                        .withSeparator(',')
                        .build();

        for (AnnotationSession annotationSession : survey.getAnnotationSessions()) {
            Long annotationSessionId = annotationSession.getId();
            Long annotatorId = annotationSession.getAnnotator().getId();

            for (UserAnnotation userAnnotation : annotationSession.getUserAnnotations()) {
                ExportAnnotationRow row = new ExportAnnotationRow();
                row.setAnnotationSessionId(annotationSessionId);
                row.setAnnotatorId(annotatorId);
                row.setCharUid(userAnnotation.getCharacterBoundingBox().getForeignId());
                row.setFixationUid(userAnnotation.getFixation().getForeignId());

                csv.write(row);
            }
        }


        return stringWriter.toString().getBytes(StandardCharsets.UTF_8);
    }


}
