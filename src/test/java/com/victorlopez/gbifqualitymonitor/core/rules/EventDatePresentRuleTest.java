package com.victorlopez.gbifqualitymonitor.core.rules;

import com.victorlopez.gbifqualitymonitor.core.rules.impl.EventDatePresentRule;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventDatePresentRuleTest {

    private final QualityRule rule = new EventDatePresentRule();

    @Test
    void shouldPassWhenEventDateIsPresent() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .eventDate("2024-06-15")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getRuleId()).isEqualTo("EVENT_DATE_PRESENT");
    }

    @Test
    void shouldFailWhenEventDateIsNull() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("2")
                .eventDate(null)
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getFailureReason()).isNotBlank();
    }

    @Test
    void shouldFailWhenEventDateIsBlank() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("3")
                .eventDate("   ")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenEventDateIsEmptyString() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("4")
                .eventDate("")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }
}