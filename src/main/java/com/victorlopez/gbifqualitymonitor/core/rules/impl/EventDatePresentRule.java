package com.victorlopez.gbifqualitymonitor.core.rules.impl;

import com.victorlopez.gbifqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;

public class EventDatePresentRule implements QualityRule {

    private static final String RULE_ID = "EVENT_DATE_PRESENT";

    @Override
    public RuleResult evaluate(NormalizedOccurrence occurrence) {
        if (occurrence.getEventDate() == null || occurrence.getEventDate().isBlank()) {
            return RuleResult.fail(RULE_ID, "Event date is missing or blank");
        }
        return RuleResult.pass(RULE_ID);
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }
}
