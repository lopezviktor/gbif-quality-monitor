package com.victorlopez.gibfqualitymonitor.core.rules;

import com.victorlopez.gibfqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gibfqualitymonitor.domain.model.RuleResult;

import java.util.ArrayList;
import java.util.List;

public class RuleEngine {

    private final List<QualityRule> rules;

    public RuleEngine(List<QualityRule> rules) {
        this.rules = rules;
    }

    public List<RuleResult> evaluate(List<NormalizedOccurrence> occurrences) {
        List<RuleResult> results = new ArrayList<>();
        for (NormalizedOccurrence occurrence : occurrences) {
            for (QualityRule rule : rules) {
                results.add(rule.evaluate(occurrence));
            }
        }
        return results;
    }
}
