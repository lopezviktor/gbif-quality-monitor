package com.victorlopez.gbifqualitymonitor.domain.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ScoringResult {

    private final double score;
    private final String grade;

    // Score breakdown by dimension
    private final double geographicScore;
    private final double temporalScore;
    private final double taxonomicScore;
    private final double metadataScore;
}