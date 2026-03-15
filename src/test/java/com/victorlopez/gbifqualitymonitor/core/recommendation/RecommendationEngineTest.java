package com.victorlopez.gbifqualitymonitor.core.recommendation;

import com.victorlopez.gbifqualitymonitor.domain.model.QualityMetrics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationEngineTest {

    private final RecommendationEngine engine = new RecommendationEngine();

    /**
     * Baseline metrics that trigger no recommendation rule.
     * Override individual fields per test to isolate each rule.
     * coordinatesCoverage=75 (not <60, not >=80), geospatialIssueRatio=5 (not >=15, not >=20),
     * eventDateCoverage=75, taxonRankAtSpeciesLevel=50, issueRatio=0
     */
    private QualityMetrics.QualityMetricsBuilder baseline() {
        return QualityMetrics.builder()
                .coordinatesCoverage(75.0)
                .geospatialIssueRatio(5.0)
                .eventDateCoverage(75.0)
                .temporalIssueRatio(0.0)
                .taxonRankAtSpeciesLevel(50.0)
                .countryCoverage(80.0)
                .basisOfRecordCoverage(80.0)
                .totalRecords(100)
                .recordsWithAnyIssue(0);
    }

    // ── Rule 1: coordinatesCoverage < 60 ─────────────────────────────────────

    @Test
    void shouldRecommendLowCoordinateCoverageWhenBelowThreshold() {
        QualityMetrics metrics = baseline().coordinatesCoverage(59.9).build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).contains(
                "Limited suitability for map-based analysis due to low coordinate coverage"
        );
    }

    @Test
    void shouldNotRecommendLowCoordinateCoverageWhenAtThreshold() {
        QualityMetrics metrics = baseline().coordinatesCoverage(60.0).build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).doesNotContain(
                "Limited suitability for map-based analysis due to low coordinate coverage"
        );
    }

    // ── Rule 2: coordinatesCoverage >= 80 AND geospatialIssueRatio < 20 ──────

    @Test
    void shouldRecommendSuitableForGeographicExplorationWhenBothConditionsMet() {
        QualityMetrics metrics = baseline()
                .coordinatesCoverage(80.0)
                .geospatialIssueRatio(19.9)
                .build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).contains("Suitable for basic geographic exploration");
    }

    @Test
    void shouldNotRecommendSuitableForGeographicExplorationWhenCoverageTooLow() {
        QualityMetrics metrics = baseline()
                .coordinatesCoverage(79.9)
                .geospatialIssueRatio(0.0)
                .build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).doesNotContain("Suitable for basic geographic exploration");
    }

    @Test
    void shouldNotRecommendSuitableForGeographicExplorationWhenIssueRatioTooHigh() {
        QualityMetrics metrics = baseline()
                .coordinatesCoverage(90.0)
                .geospatialIssueRatio(20.0)
                .build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).doesNotContain("Suitable for basic geographic exploration");
    }

    // ── Rule 3: eventDateCoverage < 60 ──────────────────────────────────────

    @Test
    void shouldRecommendCautionForTemporalAnalysisWhenEventDateCoverageLow() {
        QualityMetrics metrics = baseline().eventDateCoverage(59.9).build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).contains(
                "Use caution for temporal analysis because event date coverage is limited"
        );
    }

    @Test
    void shouldNotRecommendCautionForTemporalAnalysisWhenEventDateCoverageAtThreshold() {
        QualityMetrics metrics = baseline().eventDateCoverage(60.0).build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).doesNotContain(
                "Use caution for temporal analysis because event date coverage is limited"
        );
    }

    // ── Rule 4: geospatialIssueRatio >= 15 ───────────────────────────────────

    @Test
    void shouldRecommendFilteringGeospatialIssuesWhenRatioAtThreshold() {
        QualityMetrics metrics = baseline().geospatialIssueRatio(15.0).build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).contains(
                "Consider filtering records with coordinates-related issues before spatial use"
        );
    }

    @Test
    void shouldNotRecommendFilteringGeospatialIssuesWhenRatioBelowThreshold() {
        QualityMetrics metrics = baseline().geospatialIssueRatio(14.9).build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).doesNotContain(
                "Consider filtering records with coordinates-related issues before spatial use"
        );
    }

    // ── Rule 5: issueRatio (recordsWithAnyIssue / totalRecords * 100) >= 30 ──

    @Test
    void shouldRecommendReviewingIssuesWhenIssueRatioAtThreshold() {
        QualityMetrics metrics = baseline()
                .totalRecords(100)
                .recordsWithAnyIssue(30)
                .build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).contains(
                "Review records with issues before downstream analysis"
        );
    }

    @Test
    void shouldNotRecommendReviewingIssuesWhenIssueRatioBelowThreshold() {
        QualityMetrics metrics = baseline()
                .totalRecords(100)
                .recordsWithAnyIssue(29)
                .build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).doesNotContain(
                "Review records with issues before downstream analysis"
        );
    }

    @Test
    void shouldNotRecommendReviewingIssuesWhenTotalRecordsIsZero() {
        QualityMetrics metrics = baseline()
                .totalRecords(0)
                .recordsWithAnyIssue(0)
                .build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).doesNotContain(
                "Review records with issues before downstream analysis"
        );
    }

    // ── Rule 6: taxonRankAtSpeciesLevel >= 95 ─────────────────────────────────

    @Test
    void shouldRecommendStrongTaxonomicCoverageWhenAtThreshold() {
        QualityMetrics metrics = baseline().taxonRankAtSpeciesLevel(95.0).build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).contains(
                "Taxonomic naming coverage is strong for basic occurrence review"
        );
    }

    @Test
    void shouldNotRecommendStrongTaxonomicCoverageWhenBelowThreshold() {
        QualityMetrics metrics = baseline().taxonRankAtSpeciesLevel(94.9).build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).doesNotContain(
                "Taxonomic naming coverage is strong for basic occurrence review"
        );
    }

    // ── Multiple recommendations ──────────────────────────────────────────────

    @Test
    void shouldReturnMultipleRecommendationsWhenSeveralRulesFire() {
        // coordinatesCoverage < 60 → rule 1
        // eventDateCoverage < 60 → rule 3
        // taxonRankAtSpeciesLevel >= 95 → rule 6
        QualityMetrics metrics = baseline()
                .coordinatesCoverage(40.0)
                .eventDateCoverage(30.0)
                .taxonRankAtSpeciesLevel(97.0)
                .build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).containsExactlyInAnyOrder(
                "Limited suitability for map-based analysis due to low coordinate coverage",
                "Use caution for temporal analysis because event date coverage is limited",
                "Taxonomic naming coverage is strong for basic occurrence review"
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoRuleFires() {
        QualityMetrics metrics = baseline().build();

        List<String> recommendations = engine.recommend(metrics);

        assertThat(recommendations).isEmpty();
    }
}
