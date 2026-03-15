package com.victorlopez.gbifqualitymonitor.application.usecase;

import com.victorlopez.gbifqualitymonitor.domain.model.AnalysisReport;
import com.victorlopez.gbifqualitymonitor.infrastructure.persistence.mapper.PersistenceMapper;
import com.victorlopez.gbifqualitymonitor.infrastructure.persistence.repository.AnalysisReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FindAnalysisReportsUseCase {

    private final AnalysisReportRepository repository;
    private final PersistenceMapper persistenceMapper;

    public FindAnalysisReportsUseCase(AnalysisReportRepository repository, PersistenceMapper persistenceMapper) {
        this.repository       = repository;
        this.persistenceMapper = persistenceMapper;
    }

    public List<AnalysisReport> findAll() {
        return repository.findAll().stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    public Optional<AnalysisReport> findById(UUID id) {
        return repository.findById(id).map(persistenceMapper::toDomain);
    }
}
