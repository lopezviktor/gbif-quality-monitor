package com.victorlopez.gbifqualitymonitor.core.rules;

import com.victorlopez.gbifqualitymonitor.core.rules.impl.CoordinatesPresentRule;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoordinatesPresentRuleTest {

    private final QualityRule rule = new CoordinatesPresentRule();

    @Test
    void shouldPassWhenBothCoordinatesArePresent() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .decimalLatitude(40.416775)
                .decimalLongitude(-3.703790)
                .issues(java.util.List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getRuleId()).isEqualTo("COORDINATES_PRESENT");
    }

    @Test
    void shouldFailWhenLatitudeIsNull() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("2")
                .decimalLatitude(null)
                .decimalLongitude(-3.703790)
                .issues(java.util.List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getFailureReason()).isNotBlank();
    }

    @Test
    void shouldFailWhenBothCoordinatesAreNull() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("3")
                .decimalLatitude(null)
                .decimalLongitude(null)
                .issues(java.util.List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenLongitudeIsNull() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("4")
                .decimalLatitude(40.416775)
                .decimalLongitude(null)
                .issues(java.util.List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }
}