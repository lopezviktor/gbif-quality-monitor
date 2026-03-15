package com.victorlopez.gbifqualitymonitor.infrastructure.persistence.repository;

import com.victorlopez.gbifqualitymonitor.infrastructure.persistence.entity.AnalysisReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReportEntity, UUID> {
}
