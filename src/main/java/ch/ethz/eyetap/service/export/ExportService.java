package ch.ethz.eyetap.service.export;

import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.Reader;
import ch.ethz.eyetap.model.annotation.Text;
import ch.ethz.eyetap.model.annotation.UserAnnotation;
import ch.ethz.eyetap.model.survey.Survey;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@Transactional
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
            Text text = annotationSession.getReadingSession().getText();
            Reader reader = annotationSession.getReadingSession().getReader();

            log.info("Writing data of annotation session {} by annotator {}", annotationSessionId, annotatorId);
            for (UserAnnotation userAnnotation : annotationSession.getUserAnnotations()) {
                ExportAnnotationRow row = new ExportAnnotationRow();
                row.setAnnotationSessionId(annotationSessionId);
                row.setAnnotatorId(annotatorId);
                row.setCharUid(userAnnotation.getCharacterBoundingBox().getForeignId());
                row.setFixationUid(userAnnotation.getFixation().getForeignId());
                row.setTextUid(text.getForeignId());
                row.setTextLang(text.getLanguage());
                row.setReaderUid(reader.getForeignId());
                csv.write(row);
                stringWriter.flush();
            }
        }
        log.info("CSV content {}", stringWriter);

        return stringWriter.toString().getBytes(StandardCharsets.UTF_8);
    }


}
