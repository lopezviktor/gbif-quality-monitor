package com.victorlopez.gbifqualitymonitor.core.rules.impl;

import com.victorlopez.gbifqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;

public class BasisOfRecordPresentRule implements QualityRule {

    private static final String RULE_ID = "BASIS_OF_RECORD_PRESENT";

    @Override
    public RuleResult evaluate(NormalizedOccurrence occurrence) {
        if(occurrence.getBasisOfRecord() == null || occurrence.getBasisOfRecord().isBlank()) {
            return RuleResult.fail(RULE_ID, "Basis of record is empty or missing");
        }
        return RuleResult.pass(RULE_ID);
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }

}
