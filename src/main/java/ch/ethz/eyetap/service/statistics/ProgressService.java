package ch.ethz.eyetap.service.statistics;

import ch.ethz.eyetap.dto.progress.OverallProgressStatisticsDto;
import ch.ethz.eyetap.dto.progress.ProgressDto;
import ch.ethz.eyetap.dto.progress.ReadingSessionProgressDto;
import ch.ethz.eyetap.model.annotation.AnnotationSession;
import ch.ethz.eyetap.model.annotation.ReadingSession;
import ch.ethz.eyetap.repository.*;
import ch.ethz.eyetap.service.TextService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProgressService {

    private final ReadingSessionRepository readingSessionRepository;
    private final SurveyRepository surveyRepository;
    private final AnnotationSessionRepository annotationSessionRepository;
    private final TextRepository textRepository;
    private final AnnotatorRepository annotatorRepository;
    private final UserAnnotationRepository userAnnotationRepository;
    private final TextService textService;

    private volatile ProgressDto currentProgress;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        refreshProgress();
    }


    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void refreshProgress() {
        log.info("Refreshing progress statistics");

        long start = System.currentTimeMillis();

        currentProgress = calculateProgress();

        log.info("Progress statistics recalculated in {} seconds",
                (System.currentTimeMillis() - start) / 1000.0);
    }

    public ProgressDto getProgress() {
        return currentProgress;
    }

    public ProgressDto calculateProgress() {

        return ProgressDto.builder()
                .statisticsDto(this.calculateStatistics())
                .progress(this.calculatePerReadingSessionProgress())
                .build();
    }

    private OverallProgressStatisticsDto calculateStatistics() {
        return OverallProgressStatisticsDto.builder()
                .numberOfReadingSessions(
                        Math.toIntExact(this.readingSessionRepository.count())
                )
                .numberOfSurveys(Math.toIntExact(this.surveyRepository.count()))
                .numberOfTexts(Math.toIntExact(this.textRepository.count()))
                .numberOfUniqueAnnotators(Math.toIntExact(this.annotatorRepository.count()))
                .numberOfAnnotations(Math.toIntExact(this.userAnnotationRepository.count()))
                .progressUntilEverythingIsAnnotatedOnce(this.calculateProgressUntilEverythingIsAnnotatedOnce())
                .build();
    }

    private Double calculateProgressUntilEverythingIsAnnotatedOnce() {
        int totalFixations = (int) this.readingSessionRepository.countAllFixations();

        Set<Long> fixationsThatAreAnnotated =
                userAnnotationRepository.findAnnotatedFixationIds();

        fixationsThatAreAnnotated.addAll(
                annotationSessionRepository.findInvalidFixationIds()
        );

        return ((double) fixationsThatAreAnnotated.size()) / totalFixations;
    }

    private Map<ProgressDto.ProgressKey, ReadingSessionProgressDto> calculatePerReadingSessionProgress() {

        Map<Long, Long> fixationCounts = getFixationCounts();

        Map<ProgressDto.ProgressKey, ReadingSessionProgressDto> result = new HashMap<>();
        for (ReadingSession readingSession : this.readingSessionRepository.findAll()) {
            ProgressDto.ProgressKey key = new ProgressDto.ProgressKey(readingSession.getId(),
                    this.textService.toShallowTextDto(readingSession.getText()),
                    readingSession.getText().getLanguage());

            result.put(key, this.calculateReadingSessionProgress(fixationCounts.get(readingSession.getId()).intValue(),
                    readingSession));

        }

        return result;
    }

    private Map<Long, Long> getFixationCounts() {
        return readingSessionRepository.findFixationCountsByReadingSession()
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));
    }

    private ReadingSessionProgressDto calculateReadingSessionProgress(Integer totalFixations, ReadingSession readingSession) {
        List<AnnotationSession> annotationSessions = this.annotationSessionRepository.findAnnotationSessionsByReadingSession(readingSession);

        int annotations = annotationSessions.stream().mapToInt(annotationSession -> annotationSession.getUserAnnotations().size()
                        + annotationSession.getFixationsMarkedInvalid().size())
                .sum();
        double ratio = ((double) annotations) / totalFixations;

        int uniqueAnnotators = annotationSessions.stream().map(annotationSession -> annotationSession.getAnnotator().getId())
                .collect(Collectors.toSet()).size();

        return new ReadingSessionProgressDto(ratio,
                uniqueAnnotators);

    }


}
