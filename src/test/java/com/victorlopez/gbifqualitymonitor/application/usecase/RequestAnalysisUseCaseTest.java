package com.victorlopez.gbifqualitymonitor.application.usecase;

import com.victorlopez.gbifqualitymonitor.core.metrics.MetricsAggregator;
import com.victorlopez.gbifqualitymonitor.core.normalizer.OccurrenceNormalizer;
import com.victorlopez.gbifqualitymonitor.core.recommendation.RecommendationEngine;
import com.victorlopez.gbifqualitymonitor.core.rules.RuleEngine;
import com.victorlopez.gbifqualitymonitor.core.scoring.ScoreCalculator;
import com.victorlopez.gbifqualitymonitor.domain.model.AnalysisReport;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.QualityMetrics;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;
import com.victorlopez.gbifqualitymonitor.domain.model.ScoringResult;
import com.victorlopez.gbifqualitymonitor.gbif.client.GbifApiException;
import com.victorlopez.gbifqualitymonitor.gbif.client.GbifClient;
import com.victorlopez.gbifqualitymonitor.gbif.dto.GbifOccurrence;
import com.victorlopez.gbifqualitymonitor.gbif.dto.GbifOccurrenceResponse;
import com.victorlopez.gbifqualitymonitor.infrastructure.persistence.entity.AnalysisReportEntity;
import com.victorlopez.gbifqualitymonitor.infrastructure.persistence.mapper.PersistenceMapper;
import com.victorlopez.gbifqualitymonitor.infrastructure.persistence.repository.AnalysisReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestAnalysisUseCaseTest {

    @Mock private GbifClient gbifClient;
    @Mock private OccurrenceNormalizer occurrenceNormalizer;
    @Mock private RuleEngine ruleEngine;
    @Mock private MetricsAggregator metricsAggregator;
    @Mock private ScoreCalculator scoreCalculator;
    @Mock private RecommendationEngine recommendationEngine;
    @Mock private PersistenceMapper persistenceMapper;
    @Mock private AnalysisReportRepository repository;

    @InjectMocks
    private RequestAnalysisUseCase useCase;

    // ── shared fixtures ───────────────────────────────────────────────────────

    private GbifOccurrence gbifOccurrence() {
        return GbifOccurrence.builder()
                .gbifID("1001")
                .scientificName("Canis lupus")
                .issues(List.of())
                .media(List.of())
                .build();
    }

    private NormalizedOccurrence normalizedOccurrence() {
        return NormalizedOccurrence.builder()
                .gbifId("1001")
                .scientificName("Canis lupus")
                .issues(List.of())
                .build();
    }

    private QualityMetrics someMetrics() {
        return QualityMetrics.builder()
                .coordinatesCoverage(80.0).geospatialIssueRatio(5.0)
                .eventDateCoverage(75.0).temporalIssueRatio(0.0)
                .taxonRankAtSpeciesLevel(90.0)
                .countryCoverage(85.0).basisOfRecordCoverage(100.0)
                .totalRecords(5).recordsWithAnyIssue(1)
                .build();
    }

    private ScoringResult someScoring() {
        return ScoringResult.builder()
                .score(74.3).grade("B")
                .geographicScore(28.1).temporalScore(18.4)
                .taxonomicScore(19.2).metadataScore(8.6)
                .build();
    }

    // ── happy path ────────────────────────────────────────────────────────────

    @Test
    void shouldOrchestrateFulPipelineAndSaveReport() {
        List<GbifOccurrence> rawList  = List.of(gbifOccurrence());
        List<NormalizedOccurrence> normalizedList = List.of(normalizedOccurrence());
        List<RuleResult> ruleResults  = List.of(RuleResult.pass("COORDINATES_PRESENT"));
        QualityMetrics metrics        = someMetrics();
        ScoringResult scoring         = someScoring();
        List<String> recommendations  = List.of("Consider filtering geospatial issues");

        GbifOccurrenceResponse response = GbifOccurrenceResponse.builder()
                .count(1).limit(300).results(rawList).build();
        AnalysisReportEntity entity      = AnalysisReportEntity.builder().build();
        AnalysisReportEntity savedEntity = AnalysisReportEntity.builder().build();
        AnalysisReport savedReport = AnalysisReport.builder()
                .taxonKey(5219173L).completenessScore(74.3).scoreGrade("B")
                .metrics(Map.of()).recommendations(List.of()).scoreBreakdown(Map.of())
                .build();

        when(gbifClient.fetchOccurrences(5219173L, 300, "ES")).thenReturn(response);
        when(occurrenceNormalizer.normalize(rawList)).thenReturn(normalizedList);
        when(ruleEngine.evaluate(normalizedList)).thenReturn(ruleResults);
        when(metricsAggregator.aggregate(ruleResults, 1)).thenReturn(metrics);
        when(scoreCalculator.calculate(metrics)).thenReturn(scoring);
        when(recommendationEngine.recommend(metrics)).thenReturn(recommendations);
        when(persistenceMapper.toEntity(any())).thenReturn(entity);
        when(repository.save(entity)).thenReturn(savedEntity);
        when(persistenceMapper.toDomain(savedEntity)).thenReturn(savedReport);

        AnalysisReport result = useCase.execute(5219173L, 300, "ES");

        verify(repository).save(entity);
        assertThat(result).isEqualTo(savedReport);
    }

    @Test
    void shouldPassCorrectScoreAndGradeToEntityMapper() {
        List<GbifOccurrence> rawList = List.of(gbifOccurrence());
        List<NormalizedOccurrence> normalizedList = List.of(normalizedOccurrence());
        QualityMetrics metrics   = someMetrics();
        ScoringResult scoring    = someScoring();

        when(gbifClient.fetchOccurrences(any(), any(), any()))
                .thenReturn(GbifOccurrenceResponse.builder().count(1).results(rawList).build());
        when(occurrenceNormalizer.normalize(anyList())).thenReturn(normalizedList);
        when(ruleEngine.evaluate(anyList())).thenReturn(List.of());
        when(metricsAggregator.aggregate(anyList(), anyInt())).thenReturn(metrics);
        when(scoreCalculator.calculate(any())).thenReturn(scoring);
        when(recommendationEngine.recommend(any())).thenReturn(List.of());
        when(persistenceMapper.toEntity(any())).thenReturn(AnalysisReportEntity.builder().build());
        when(repository.save(any())).thenReturn(AnalysisReportEntity.builder().build());
        when(persistenceMapper.toDomain(any())).thenReturn(
                AnalysisReport.builder().metrics(Map.of()).recommendations(List.of()).scoreBreakdown(Map.of()).build());

        useCase.execute(5219173L, 300, "ES");

        ArgumentCaptor<AnalysisReport> captor = ArgumentCaptor.forClass(AnalysisReport.class);
        verify(persistenceMapper).toEntity(captor.capture());
        AnalysisReport captured = captor.getValue();

        assertThat(captured.getCompletenessScore()).isEqualTo(74.3);
        assertThat(captured.getScoreGrade()).isEqualTo("B");
        assertThat(captured.getRecordsAnalyzed()).isEqualTo(1);
        assertThat(captured.getReturnedByGbif()).isEqualTo(1);
        assertThat(captured.getScientificName()).isEqualTo("Canis lupus");
        assertThat(captured.getTaxonKey()).isEqualTo(5219173L);
        assertThat(captured.getCountry()).isEqualTo("ES");
    }

    // ── zero records ──────────────────────────────────────────────────────────

    @Test
    void shouldReturnZeroScoreReportAndSkipPipelineWhenGbifReturnsNoRecords() {
        when(gbifClient.fetchOccurrences(5219173L, 300, "ES"))
                .thenReturn(GbifOccurrenceResponse.builder().count(0).results(List.of()).build());
        when(occurrenceNormalizer.normalize(List.of())).thenReturn(List.of());
        when(persistenceMapper.toEntity(any())).thenReturn(AnalysisReportEntity.builder().build());
        when(repository.save(any())).thenReturn(AnalysisReportEntity.builder().build());
        when(persistenceMapper.toDomain(any())).thenReturn(
                AnalysisReport.builder().metrics(Map.of()).recommendations(List.of()).scoreBreakdown(Map.of()).build());

        useCase.execute(5219173L, 300, "ES");

        verify(ruleEngine, never()).evaluate(any());
        verify(metricsAggregator, never()).aggregate(any(), anyInt());
        verify(scoreCalculator, never()).calculate(any());
        verify(recommendationEngine, never()).recommend(any());
    }

    @Test
    void shouldSaveZeroScoreReportWithCorrectFields() {
        when(gbifClient.fetchOccurrences(any(), any(), any()))
                .thenReturn(GbifOccurrenceResponse.builder().count(0).results(List.of()).build());
        when(occurrenceNormalizer.normalize(List.of())).thenReturn(List.of());
        when(persistenceMapper.toEntity(any())).thenReturn(AnalysisReportEntity.builder().build());
        when(repository.save(any())).thenReturn(AnalysisReportEntity.builder().build());
        when(persistenceMapper.toDomain(any())).thenReturn(
                AnalysisReport.builder().metrics(Map.of()).recommendations(List.of()).scoreBreakdown(Map.of()).build());

        useCase.execute(5219173L, 300, "ES");

        ArgumentCaptor<AnalysisReport> captor = ArgumentCaptor.forClass(AnalysisReport.class);
        verify(persistenceMapper).toEntity(captor.capture());
        AnalysisReport captured = captor.getValue();

        assertThat(captured.getCompletenessScore()).isEqualTo(0.0);
        assertThat(captured.getScoreGrade()).isEqualTo("F");
        assertThat(captured.getRecordsAnalyzed()).isEqualTo(0);
        assertThat(captured.getRecommendations()).isEmpty();
        assertThat(captured.getMetrics()).isEmpty();
        assertThat(captured.getScoreBreakdown()).isEmpty();
    }

    @Test
    void shouldStillSaveReportWhenGbifReturnsNoRecords() {
        when(gbifClient.fetchOccurrences(any(), any(), any()))
                .thenReturn(GbifOccurrenceResponse.builder().count(0).results(List.of()).build());
        when(occurrenceNormalizer.normalize(List.of())).thenReturn(List.of());
        when(persistenceMapper.toEntity(any())).thenReturn(AnalysisReportEntity.builder().build());
        when(repository.save(any())).thenReturn(AnalysisReportEntity.builder().build());
        when(persistenceMapper.toDomain(any())).thenReturn(
                AnalysisReport.builder().metrics(Map.of()).recommendations(List.of()).scoreBreakdown(Map.of()).build());

        useCase.execute(5219173L, 300, "ES");

        verify(repository).save(any());
    }

    // ── exception propagation ─────────────────────────────────────────────────

    @Test
    void shouldPropagateGbifApiExceptionWithoutSwallowing() {
        when(gbifClient.fetchOccurrences(any(), any(), any()))
                .thenThrow(new GbifApiException("GBIF returned HTTP 500: Internal Server Error"));

        assertThatThrownBy(() -> useCase.execute(5219173L, 300, "ES"))
                .isInstanceOf(GbifApiException.class)
                .hasMessageContaining("500");

        verify(repository, never()).save(any());
    }
}
