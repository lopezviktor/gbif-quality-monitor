package com.victorlopez.gbifqualitymonitor.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class AnalysisReportDTO {

    private final UUID reportId;
    private final Long taxonKey;
    private final String scientificName;
    private final String country;
    private final LocalDateTime requestedAt;
    private final Integer recordsAnalyzed;
    private final Integer returnedByGbif;
    private final Double completenessScore;
    private final String scoreGrade;
    private final Map<String, Double> scoreBreakdown;
    private final Map<String, Double> metrics;
    private final List<String> recommendations;
}
