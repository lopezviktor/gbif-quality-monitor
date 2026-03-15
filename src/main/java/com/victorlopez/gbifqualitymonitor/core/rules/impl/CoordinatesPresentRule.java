package com.victorlopez.gbifqualitymonitor.core.rules.impl;

import com.victorlopez.gbifqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;

public class CoordinatesPresentRule implements QualityRule {

    private static final String RULE_ID = "COORDINATES_PRESENT";

    @Override
    public RuleResult evaluate(NormalizedOccurrence occurrence) {
        if (occurrence.getDecimalLatitude() == null || occurrence.getDecimalLongitude() == null) {
            return RuleResult.fail(RULE_ID, "Latitude or longitude is missing");
        }
        return RuleResult.pass(RULE_ID);
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }
}