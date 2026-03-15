package com.victorlopez.gbifqualitymonitor.core.rules;

import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;

public interface QualityRule {

    RuleResult evaluate(NormalizedOccurrence occurrence);

    String getRuleId();
}