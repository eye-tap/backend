package ch.ethz.eyetap.service;


import ch.ethz.eyetap.dto.AnnotationsMetaDataDto;
import ch.ethz.eyetap.model.User;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.AnnotationType;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AnnotationSessionService {

    public Set<AnnotationSession> getAnnotationSessionsByUser(User user) {
        return user.getAnnotator().getAnnotationSessions();
    }

    public AnnotationsMetaDataDto calculateAnnotationsMetaData(AnnotationSession session) {
        int total = session.getAnnotations().size();
        int set = (int) session.getAnnotations().stream().filter(annotation -> annotation.getAnnotationType().equals(AnnotationType.USER))
                .count();

        return new AnnotationsMetaDataDto(total, set);
    }

}
