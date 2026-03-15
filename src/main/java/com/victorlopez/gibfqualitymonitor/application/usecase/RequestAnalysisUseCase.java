package com.victorlopez.gibfqualitymonitor.application.usecase;

import com.victorlopez.gibfqualitymonitor.core.metrics.MetricsAggregator;
import com.victorlopez.gibfqualitymonitor.core.normalizer.OccurrenceNormalizer;
import com.victorlopez.gibfqualitymonitor.core.recommendation.RecommendationEngine;
import com.victorlopez.gibfqualitymonitor.core.rules.RuleEngine;
import com.victorlopez.gibfqualitymonitor.core.scoring.ScoreCalculator;
import com.victorlopez.gibfqualitymonitor.domain.model.AnalysisReport;
import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.QualityMetrics;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;
import com.victorlopez.gibfqualitymonitor.domain.model.ScoringResult;
import com.victorlopez.gibfqualitymonitor.gbif.client.GbifClient;
import com.victorlopez.gibfqualitymonitor.gbif.dto.GbifOccurrence;
import com.victorlopez.gibfqualitymonitor.gbif.dto.GbifOccurrenceResponse;
import com.victorlopez.gibfqualitymonitor.infrastructure.persistence.entity.AnalysisReportEntity;
import com.victorlopez.gibfqualitymonitor.infrastructure.persistence.mapper.PersistenceMapper;
import com.victorlopez.gibfqualitymonitor.infrastructure.persistence.repository.AnalysisReportRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RequestAnalysisUseCase {

    private final GbifClient gbifClient;
    private final OccurrenceNormalizer occurrenceNormalizer;
    private final RuleEngine ruleEngine;
    private final MetricsAggregator metricsAggregator;
    private final ScoreCalculator scoreCalculator;
    private final RecommendationEngine recommendationEngine;
    private final PersistenceMapper persistenceMapper;
    private final AnalysisReportRepository repository;

    public RequestAnalysisUseCase(
            GbifClient gbifClient,
            OccurrenceNormalizer occurrenceNormalizer,
            RuleEngine ruleEngine,
            MetricsAggregator metricsAggregator,
            ScoreCalculator scoreCalculator,
            RecommendationEngine recommendationEngine,
            PersistenceMapper persistenceMapper,
            AnalysisReportRepository repository) {
        this.gbifClient            = gbifClient;
        this.occurrenceNormalizer  = occurrenceNormalizer;
        this.ruleEngine            = ruleEngine;
        this.metricsAggregator     = metricsAggregator;
        this.scoreCalculator       = scoreCalculator;
        this.recommendationEngine  = recommendationEngine;
        this.persistenceMapper     = persistenceMapper;
        this.repository            = repository;
    }

    public AnalysisReport execute(Long taxonKey, Integer limit, String country) {
        GbifOccurrenceResponse response = gbifClient.fetchOccurrences(taxonKey, limit, country);

        List<GbifOccurrence> raw = response.getResults() != null ? response.getResults() : List.of();
        List<NormalizedOccurrence> normalized = occurrenceNormalizer.normalize(raw);

        AnalysisReport report = normalized.isEmpty()
                ? buildZeroReport(taxonKey, limit, country, raw.size())
                : buildReport(taxonKey, limit, country, normalized, raw.size());

        AnalysisReportEntity entity = persistenceMapper.toEntity(report);
        AnalysisReportEntity saved  = repository.save(entity);
        return persistenceMapper.toDomain(saved);
    }

    // ── private builders ──────────────────────────────────────────────────────

    private AnalysisReport buildZeroReport(Long taxonKey, Integer limit, String country, int returnedByGbif) {
        return AnalysisReport.builder()
                .taxonKey(taxonKey)
                .country(country)
                .requestedLimit(limit)
                .recordsAnalyzed(0)
                .returnedByGbif(returnedByGbif)
                .completenessScore(0.0)
                .scoreGrade("F")
                .metrics(Map.of())
                .recommendations(List.of())
                .scoreBreakdown(Map.of())
                .build();
    }

    private AnalysisReport buildReport(Long taxonKey, Integer limit, String country,
                                       List<NormalizedOccurrence> normalized, int returnedByGbif) {
        List<RuleResult> ruleResults  = ruleEngine.evaluate(normalized);
        QualityMetrics metrics        = metricsAggregator.aggregate(ruleResults, normalized.size());
        ScoringResult scoring         = scoreCalculator.calculate(metrics);
        List<String> recommendations  = recommendationEngine.recommend(metrics);

        return AnalysisReport.builder()
                .taxonKey(taxonKey)
                .scientificName(normalized.get(0).getScientificName())
                .country(country)
                .requestedLimit(limit)
                .recordsAnalyzed(normalized.size())
                .returnedByGbif(returnedByGbif)
                .completenessScore(scoring.getScore())
                .scoreGrade(scoring.getGrade())
                .metrics(toMetricsMap(metrics))
                .recommendations(recommendations)
                .scoreBreakdown(toScoreBreakdownMap(scoring))
                .build();
    }

    private Map<String, Double> toMetricsMap(QualityMetrics m) {
        Map<String, Double> map = new LinkedHashMap<>();
        map.put("coordinatesCoverage",     m.getCoordinatesCoverage());
        map.put("geospatialIssueRatio",    m.getGeospatialIssueRatio());
        map.put("eventDateCoverage",       m.getEventDateCoverage());
        map.put("temporalIssueRatio",      m.getTemporalIssueRatio());
        map.put("taxonRankAtSpeciesLevel",  m.getTaxonRankAtSpeciesLevel());
        map.put("countryCoverage",         m.getCountryCoverage());
        map.put("basisOfRecordCoverage",   m.getBasisOfRecordCoverage());
        map.put("recordsWithAnyIssue",     (double) m.getRecordsWithAnyIssue());
        return map;
    }

    private Map<String, Double> toScoreBreakdownMap(ScoringResult s) {
        return Map.of(
                "geographicScore", s.getGeographicScore(),
                "temporalScore",   s.getTemporalScore(),
                "taxonomicScore",  s.getTaxonomicScore(),
                "metadataScore",   s.getMetadataScore()
        );
    }
}
