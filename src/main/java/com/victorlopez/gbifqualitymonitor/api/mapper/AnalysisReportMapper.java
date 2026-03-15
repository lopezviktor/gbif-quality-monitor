package com.victorlopez.gbifqualitymonitor.api.mapper;

import com.victorlopez.gbifqualitymonitor.api.dto.AnalysisReportDTO;
import com.victorlopez.gbifqualitymonitor.api.dto.AnalysisReportSummaryDTO;
import com.victorlopez.gbifqualitymonitor.domain.model.AnalysisReport;
import org.springframework.stereotype.Component;

@Component
public class AnalysisReportMapper {

    public AnalysisReportDTO toDTO(AnalysisReport report) {
        return AnalysisReportDTO.builder()
                .reportId(report.getId())
                .taxonKey(report.getTaxonKey())
                .scientificName(report.getScientificName())
                .country(report.getCountry())
                .requestedAt(report.getCreatedAt())
                .recordsAnalyzed(report.getRecordsAnalyzed())
                .returnedByGbif(report.getReturnedByGbif())
                .completenessScore(report.getCompletenessScore())
                .scoreGrade(report.getScoreGrade())
                .scoreBreakdown(report.getScoreBreakdown())
                .metrics(report.getMetrics())
                .recommendations(report.getRecommendations())
                .build();
    }

    public AnalysisReportSummaryDTO toSummaryDTO(AnalysisReport report) {
        return AnalysisReportSummaryDTO.builder()
                .reportId(report.getId())
                .taxonKey(report.getTaxonKey())
                .scientificName(report.getScientificName())
                .requestedAt(report.getCreatedAt())
                .recordsAnalyzed(report.getRecordsAnalyzed())
                .completenessScore(report.getCompletenessScore())
                .scoreGrade(report.getScoreGrade())
                .build();
    }
}
