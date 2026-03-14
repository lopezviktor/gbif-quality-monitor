package com.victorlopez.gibfqualitymonitor.core.rules;

import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;

public interface QualityRule {

    RuleResult evaluate(NormalizedOccurrence occurrence);

    String getRuleId();
}