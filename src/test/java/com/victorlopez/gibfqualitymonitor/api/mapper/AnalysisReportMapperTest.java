package com.victorlopez.gibfqualitymonitor.api.mapper;

import com.victorlopez.gibfqualitymonitor.api.dto.AnalysisReportDTO;
import com.victorlopez.gibfqualitymonitor.api.dto.AnalysisReportSummaryDTO;
import com.victorlopez.gibfqualitymonitor.domain.model.AnalysisReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AnalysisReportMapperTest {

    private AnalysisReportMapper mapper;

    private AnalysisReport report;
    private final UUID id = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.of(2025, 6, 1, 12, 0);

    @BeforeEach
    void setUp() {
        mapper = new AnalysisReportMapper();
        report = AnalysisReport.builder()
                .id(id)
                .taxonKey(1234567L)
                .scientificName("Panthera leo")
                .country("ZA")
                .requestedLimit(100)
                .recordsAnalyzed(80)
                .returnedByGbif(80)
                .completenessScore(72.5)
                .scoreGrade("B")
                .metrics(Map.of("coordinatesCoverage", 90.0, "eventDateCoverage", 75.0))
                .scoreBreakdown(Map.of("geographicScore", 30.0, "temporalScore", 18.0))
                .recommendations(List.of("Suitable for basic geographic exploration"))
                .createdAt(now)
                .build();
    }

    // ── toDTO ─────────────────────────────────────────────────────────────────

    @Test
    void toDTO_mapsReportIdFromDomainId() {
        AnalysisReportDTO dto = mapper.toDTO(report);
        assertThat(dto.getReportId()).isEqualTo(id);
    }

    @Test
    void toDTO_mapsTaxonKey() {
        AnalysisReportDTO dto = mapper.toDTO(report);
        assertThat(dto.getTaxonKey()).isEqualTo(1234567L);
    }

    @Test
    void toDTO_mapsScientificName() {
        AnalysisReportDTO dto = mapper.toDTO(report);
        assertThat(dto.getScientificName()).isEqualTo("Panthera leo");
    }

    @Test
    void toDTO_mapsRequestedAtFromCreatedAt() {
        AnalysisReportDTO dto = mapper.toDTO(report);
        assertThat(dto.getRequestedAt()).isEqualTo(now);
    }

    @Test
    void toDTO_mapsScoreFields() {
        AnalysisReportDTO dto = mapper.toDTO(report);
        assertThat(dto.getCompletenessScore()).isEqualTo(72.5);
        assertThat(dto.getScoreGrade()).isEqualTo("B");
    }

    @Test
    void toDTO_mapsMetricsAndScoreBreakdown() {
        AnalysisReportDTO dto = mapper.toDTO(report);
        assertThat(dto.getMetrics()).containsKey("coordinatesCoverage");
        assertThat(dto.getScoreBreakdown()).containsKey("geographicScore");
    }

    @Test
    void toDTO_mapsRecommendations() {
        AnalysisReportDTO dto = mapper.toDTO(report);
        assertThat(dto.getRecommendations()).containsExactly("Suitable for basic geographic exploration");
    }

    @Test
    void toDTO_mapsRecordCounts() {
        AnalysisReportDTO dto = mapper.toDTO(report);
        assertThat(dto.getRecordsAnalyzed()).isEqualTo(80);
        assertThat(dto.getReturnedByGbif()).isEqualTo(80);
    }

    // ── toSummaryDTO ──────────────────────────────────────────────────────────

    @Test
    void toSummaryDTO_mapsReportIdAndTaxonKey() {
        AnalysisReportSummaryDTO dto = mapper.toSummaryDTO(report);
        assertThat(dto.getReportId()).isEqualTo(id);
        assertThat(dto.getTaxonKey()).isEqualTo(1234567L);
    }

    @Test
    void toSummaryDTO_mapsScientificNameAndGrade() {
        AnalysisReportSummaryDTO dto = mapper.toSummaryDTO(report);
        assertThat(dto.getScientificName()).isEqualTo("Panthera leo");
        assertThat(dto.getScoreGrade()).isEqualTo("B");
    }

    @Test
    void toSummaryDTO_mapsRequestedAtFromCreatedAt() {
        AnalysisReportSummaryDTO dto = mapper.toSummaryDTO(report);
        assertThat(dto.getRequestedAt()).isEqualTo(now);
    }

    @Test
    void toSummaryDTO_mapsRecordsAnalyzedAndScore() {
        AnalysisReportSummaryDTO dto = mapper.toSummaryDTO(report);
        assertThat(dto.getRecordsAnalyzed()).isEqualTo(80);
        assertThat(dto.getCompletenessScore()).isEqualTo(72.5);
    }
}
