package com.victorlopez.gbifqualitymonitor.core.rules.impl;

import com.victorlopez.gbifqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gbifqualitymonitor.domain.model.NormalizedOccurrence;
import com.victorlopez.gbifqualitymonitor.domain.model.RuleResult;

public class TaxonRankAtSpeciesLevelRule implements QualityRule {

    private static final String RULE_ID = "TAXON_RANK_AT_SPECIES_LEVEL";
    private static final String SPECIES = "SPECIES";

    @Override
    public RuleResult evaluate(NormalizedOccurrence occurrence) {
        if (!SPECIES.equals(occurrence.getTaxonRank())) {
            return RuleResult.fail(RULE_ID, "Taxon rank is not at species level");
        }
        return RuleResult.pass(RULE_ID);
    }

    @Override
    public String getRuleId() {
        return RULE_ID;
    }
}
