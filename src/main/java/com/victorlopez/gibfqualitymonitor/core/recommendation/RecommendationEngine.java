package com.victorlopez.gibfqualitymonitor.core.recommendation;

import com.victorlopez.gibfqualitymonitor.domain.model.QualityMetrics;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecommendationEngine {

    // Thresholds
    private static final double LOW_COORDINATE_COVERAGE_THRESHOLD    = 60.0;
    private static final double HIGH_COORDINATE_COVERAGE_THRESHOLD   = 80.0;
    private static final double LOW_GEOSPATIAL_ISSUE_RATIO_THRESHOLD = 20.0;
    private static final double HIGH_GEOSPATIAL_ISSUE_RATIO_THRESHOLD = 15.0;
    private static final double LOW_EVENT_DATE_COVERAGE_THRESHOLD    = 60.0;
    private static final double HIGH_ISSUE_RATIO_THRESHOLD           = 30.0;
    private static final double HIGH_TAXON_RANK_THRESHOLD            = 95.0;

    // Recommendation messages
    private static final String MSG_LOW_COORDINATE_COVERAGE =
            "Limited suitability for map-based analysis due to low coordinate coverage";
    private static final String MSG_SUITABLE_FOR_GEOGRAPHIC =
            "Suitable for basic geographic exploration";
    private static final String MSG_LOW_EVENT_DATE_COVERAGE =
            "Use caution for temporal analysis because event date coverage is limited";
    private static final String MSG_HIGH_GEOSPATIAL_ISSUES =
            "Consider filtering records with coordinates-related issues before spatial use";
    private static final String MSG_HIGH_ISSUE_RATIO =
            "Review records with issues before downstream analysis";
    private static final String MSG_STRONG_TAXON_RANK =
            "Taxonomic naming coverage is strong for basic occurrence review";

    public List<String> recommend(QualityMetrics metrics) {
        List<String> recommendations = new ArrayList<>();

        if (metrics.getCoordinatesCoverage() < LOW_COORDINATE_COVERAGE_THRESHOLD) {
            recommendations.add(MSG_LOW_COORDINATE_COVERAGE);
        }

        if (metrics.getCoordinatesCoverage() >= HIGH_COORDINATE_COVERAGE_THRESHOLD
                && metrics.getGeospatialIssueRatio() < LOW_GEOSPATIAL_ISSUE_RATIO_THRESHOLD) {
            recommendations.add(MSG_SUITABLE_FOR_GEOGRAPHIC);
        }

        if (metrics.getEventDateCoverage() < LOW_EVENT_DATE_COVERAGE_THRESHOLD) {
            recommendations.add(MSG_LOW_EVENT_DATE_COVERAGE);
        }

        if (metrics.getGeospatialIssueRatio() >= HIGH_GEOSPATIAL_ISSUE_RATIO_THRESHOLD) {
            recommendations.add(MSG_HIGH_GEOSPATIAL_ISSUES);
        }

        if (metrics.getTotalRecords() > 0) {
            double issueRatio = (double) metrics.getRecordsWithAnyIssue() / metrics.getTotalRecords() * 100.0;
            if (issueRatio >= HIGH_ISSUE_RATIO_THRESHOLD) {
                recommendations.add(MSG_HIGH_ISSUE_RATIO);
            }
        }

        if (metrics.getTaxonRankAtSpeciesLevel() >= HIGH_TAXON_RANK_THRESHOLD) {
            recommendations.add(MSG_STRONG_TAXON_RANK);
        }

        return recommendations;
    }
}
