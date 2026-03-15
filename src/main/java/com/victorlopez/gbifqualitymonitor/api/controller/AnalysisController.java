package com.victorlopez.gbifqualitymonitor.api.controller;

import com.victorlopez.gbifqualitymonitor.api.dto.AnalysisReportDTO;
import com.victorlopez.gbifqualitymonitor.api.dto.AnalysisReportSummaryDTO;
import com.victorlopez.gbifqualitymonitor.api.dto.AnalysisRequestDTO;
import com.victorlopez.gbifqualitymonitor.api.mapper.AnalysisReportMapper;
import com.victorlopez.gbifqualitymonitor.application.usecase.FindAnalysisReportsUseCase;
import com.victorlopez.gbifqualitymonitor.application.usecase.RequestAnalysisUseCase;
import com.victorlopez.gbifqualitymonitor.domain.model.AnalysisReport;
import com.victorlopez.gbifqualitymonitor.gbif.client.GbifApiException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analyses")
public class AnalysisController {

    private final RequestAnalysisUseCase requestAnalysisUseCase;
    private final FindAnalysisReportsUseCase findAnalysisReportsUseCase;
    private final AnalysisReportMapper analysisReportMapper;

    public AnalysisController(
            RequestAnalysisUseCase requestAnalysisUseCase,
            FindAnalysisReportsUseCase findAnalysisReportsUseCase,
            AnalysisReportMapper analysisReportMapper) {
        this.requestAnalysisUseCase    = requestAnalysisUseCase;
        this.findAnalysisReportsUseCase = findAnalysisReportsUseCase;
        this.analysisReportMapper       = analysisReportMapper;
    }

    @PostMapping
    public ResponseEntity<AnalysisReportDTO> analyze(@Valid @RequestBody AnalysisRequestDTO request) {
        AnalysisReport report = requestAnalysisUseCase.execute(
                request.getTaxonKey(),
                request.getLimit(),
                request.getCountry()
        );
        return ResponseEntity.status(201).body(analysisReportMapper.toDTO(report));
    }

    @GetMapping
    public ResponseEntity<List<AnalysisReportSummaryDTO>> getAll() {
        List<AnalysisReportSummaryDTO> summaries = findAnalysisReportsUseCase.findAll()
                .stream()
                .map(analysisReportMapper::toSummaryDTO)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalysisReportDTO> getById(@PathVariable UUID id) {
        return findAnalysisReportsUseCase.findById(id)
                .map(analysisReportMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(GbifApiException.class)
    public ResponseEntity<String> handleGbifApiException(GbifApiException ex) {
        return ResponseEntity.status(502).body("GBIF API error: " + ex.getMessage());
    }
}
