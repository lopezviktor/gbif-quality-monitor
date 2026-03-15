package com.victorlopez.gbifqualitymonitor.core.rules;

import com.victorlopez.gbifqualitymonitor.core.rules.impl.HasMediaRule;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HasMediaRuleTest {

    private final QualityRule rule = new HasMediaRule();

    @Test
    void shouldPassWhenHasMediaIsTrue() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("1")
                .hasMedia(true)
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isTrue();
        assertThat(result.getRuleId()).isEqualTo("HAS_MEDIA");
    }

    @Test
    void shouldFailWhenHasMediaIsFalse() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("2")
                .hasMedia(false)
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
        assertThat(result.getFailureReason()).isNotBlank();
    }

    @Test
    void shouldFailWhenHasMediaIsNotSet() {
        NormalizedOccurrence occurrence = NormalizedOccurrence.builder()
                .gbifId("3")
                .issues(List.of())
                .build();

        RuleResult result = rule.evaluate(occurrence);

        assertThat(result.isPassed()).isFalse();
    }
}
