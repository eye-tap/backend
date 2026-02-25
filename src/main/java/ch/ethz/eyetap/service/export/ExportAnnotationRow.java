package ch.ethz.eyetap.service.export;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class ExportAnnotationRow {
    @CsvBindByName
    private Long fixationUid;
    @CsvBindByName
    private Long charUid;
    @CsvBindByName
    private Long annotatorId;

    @CsvBindByName
    private Long annotationSessionId;

    @CsvBindByName
    private Long textUid;
    @CsvBindByName
    private String textLang;
    @CsvBindByName
    private Long readerUid;
}
