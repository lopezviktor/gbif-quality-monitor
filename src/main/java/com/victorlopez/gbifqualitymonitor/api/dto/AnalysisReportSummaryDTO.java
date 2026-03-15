package com.victorlopez.gbifqualitymonitor.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class AnalysisReportSummaryDTO {

    private final UUID reportId;
    private final Long taxonKey;
    private final String scientificName;
    private final LocalDateTime requestedAt;
    private final Integer recordsAnalyzed;
    private final Double completenessScore;
    private final String scoreGrade;
}
