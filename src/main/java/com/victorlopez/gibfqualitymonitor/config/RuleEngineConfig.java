package com.victorlopez.gibfqualitymonitor.config;

import com.victorlopez.gibfqualitymonitor.core.rules.QualityRule;
import com.victorlopez.gibfqualitymonitor.core.rules.RuleEngine;
import com.victorlopez.gibfqualitymonitor.core.rules.impl.BasisOfRecordPresentRule;
import com.victorlopez.gibfqualitymonitor.core.rules.impl.CoordinatesPresentRule;
import com.victorlopez.gibfqualitymonitor.core.rules.impl.CountryPresentRule;
import com.victorlopez.gibfqualitymonitor.core.rules.impl.EventDatePresentRule;
import com.victorlopez.gibfqualitymonitor.core.rules.impl.HasMediaRule;
import com.victorlopez.gibfqualitymonitor.core.rules.impl.NoGeospatialIssuesRule;
import com.victorlopez.gibfqualitymonitor.core.rules.impl.NoTaxonomyIssuesRule;
import com.victorlopez.gibfqualitymonitor.core.rules.impl.RecordedByPresentRule;
import com.victorlopez.gibfqualitymonitor.core.rules.impl.TaxonRankAtSpeciesLevelRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class RuleEngineConfig {

    @Bean
    public RuleEngine ruleEngine() {
        List<QualityRule> rules = List.of(
                new CoordinatesPresentRule(),
                new EventDatePresentRule(),
                new BasisOfRecordPresentRule(),
                new TaxonRankAtSpeciesLevelRule(),
                new CountryPresentRule(),
                new NoGeospatialIssuesRule(),
                new NoTaxonomyIssuesRule(),
                new RecordedByPresentRule(),
                new HasMediaRule()
        );
        return new RuleEngine(rules);
    }
}
