package com.victorlopez.gbifqualitymonitor.core.rules;

import com.victorlopez.gbifqualitymonitor.core.rules.impl.CoordinatesPresentRule;
import com.victorlopez.gbifqualitymonitor.core.rules.impl.EventDatePresentRule;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineTest {

    @Test
    void shouldReturnEmptyWhenNoOccurrences() {
        RuleEngine engine = new RuleEngine(List.of(new CoordinatesPresentRule()));

        List<RuleResult> results = engine.evaluate(List.of());

        assertThat(results).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNoRules() {
        RuleEngine engine = new RuleEngine(List.of());

        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .issues(List.of())
                .build();

        List<RuleResult> results = engine.evaluate(List.of(occurrence));

        assertThat(results).isEmpty();
    }

    @Test
    void shouldApplyOneRuleToOneOccurrence() {
        RuleEngine engine = new RuleEngine(List.of(new CoordinatesPresentRule()));

        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .decimalLatitude(40.4)
                .decimalLongitude(-3.7)
                .issues(List.of())
                .build();

        List<RuleResult> results = engine.evaluate(List.of(occurrence));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRuleId()).isEqualTo("COORDINATES_PRESENT");
        assertThat(results.get(0).isPassed()).isTrue();
    }

    @Test
    void shouldApplyMultipleRulesToOneOccurrence() {
        RuleEngine engine = new RuleEngine(List.of(
                new CoordinatesPresentRule(),
                new EventDatePresentRule()
        ));

        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .decimalLatitude(40.4)
                .decimalLongitude(-3.7)
                .eventDate("2024-06-01")
                .issues(List.of())
                .build();

        List<RuleResult> results = engine.evaluate(List.of(occurrence));

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(RuleResult::isPassed);
    }

    @Test
    void shouldApplyOneRuleToMultipleOccurrences() {
        RuleEngine engine = new RuleEngine(List.of(new CoordinatesPresentRule()));

        NormalizedOccurrence withCoords = NormalizedOccurrence.builder()
                .gbifId("1")
                .decimalLatitude(40.4)
                .decimalLongitude(-3.7)
                .issues(List.of())
                .build();

        NormalizedOccurrence withoutCoords = NormalizedOccurrence.builder()
                .gbifId("2")
                .issues(List.of())
                .build();

        List<RuleResult> results = engine.evaluate(List.of(withCoords, withoutCoords));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).isPassed()).isTrue();
        assertThat(results.get(1).isPassed()).isFalse();
    }

    @Test
    void shouldProduceTotalResultsEqualToOccurrencesTimesRules() {
        RuleEngine engine = new RuleEngine(List.of(
                new CoordinatesPresentRule(),
                new EventDatePresentRule()
        ));

        List<NormalizedOccurrence> occurrences = List.of(
                NormalizedOccurrence.builder().gbifId("1").issues(List.of()).build(),
                NormalizedOccurrence.builder().gbifId("2").issues(List.of()).build(),
                NormalizedOccurrence.builder().gbifId("3").issues(List.of()).build()
        );

        List<RuleResult> results = engine.evaluate(occurrences);

        assertThat(results).hasSize(6); // 3 occurrences × 2 rules
    }
}
