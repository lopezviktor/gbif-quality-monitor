package com.victorlopez.gbifqualitymonitor.core.rules;

import com.victorlopez.gbifqualitymonitor.core.rules.impl.BasisOfRecordPresentRule;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BasisOfRecordPresentRuleTest {

    private final QualityRule rule = new BasisOfRecordPresentRule();

    @Test
    void shouldPassWhenBasisOfRecordIsPresent() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .basisOfRecord("HUMAN_OBSERVATION")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void shouldFailWhenBasisOfRecordIsNull() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("2")
                .basisOfRecord(null)
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenBasesOfRecordIsNotPresent() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("2")
                .basisOfRecord("   ")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }

    @Test
    void shouldFailWhenBasisOfRecordIsEmpty() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("3")
                .basisOfRecord("")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }
}

