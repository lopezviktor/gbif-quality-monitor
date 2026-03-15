package com.victorlopez.gbifqualitymonitor.api.controller;

import com.victorlopez.gbifqualitymonitor.api.dto.AnalysisReportDTO;
import com.victorlopez.gbifqualitymonitor.api.dto.AnalysisReportSummaryDTO;
import com.victorlopez.gbifqualitymonitor.api.dto.AnalysisRequestDTO;
import com.victorlopez.gbifqualitymonitor.api.mapper.AnalysisReportMapper;
import com.victorlopez.gbifqualitymonitor.application.usecase.FindAnalysisReportsUseCase;
import com.victorlopez.gbifqualitymonitor.application.usecase.RequestAnalysisUseCase;
import com.victorlopez.gbifqualitymonitor.domain.model.AnalysisReport;
import com.victorlopez.gbifqualitymonitor.gbif.client.GbifApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Analyses", description = "Quality analysis operations")
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

    @Operation(summary = "Request quality analysis",
               description = "Fetches occurrence records from GBIF for the given taxon, runs all quality rules, and returns a scored report.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Analysis completed and report created"),
            @ApiResponse(responseCode = "400", description = "Invalid request — taxonKey missing or limit outside 1–500"),
            @ApiResponse(responseCode = "502", description = "GBIF API unreachable or returned a non-2xx response")
    })
    @PostMapping
    public ResponseEntity<AnalysisReportDTO> analyze(@Valid @RequestBody AnalysisRequestDTO request) {
        AnalysisReport report = requestAnalysisUseCase.execute(
                request.getTaxonKey(),
                request.getLimit(),
                request.getCountry()
        );
        return ResponseEntity.status(201).body(analysisReportMapper.toDTO(report));
    }

    @Operation(summary = "List all analyses",
               description = "Returns a summary list of all previously persisted quality analysis reports.")
    @ApiResponse(responseCode = "200", description = "List returned successfully (may be empty)")
    @GetMapping
    public ResponseEntity<List<AnalysisReportSummaryDTO>> getAll() {
        List<AnalysisReportSummaryDTO> summaries = findAnalysisReportsUseCase.findAll()
                .stream()
                .map(analysisReportMapper::toSummaryDTO)
                .toList();
        return ResponseEntity.ok(summaries);
    }

    @Operation(summary = "Get analysis report by ID",
               description = "Retrieves the full quality analysis report for the given UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report found and returned"),
            @ApiResponse(responseCode = "404", description = "No report found for the given ID")
    })
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
