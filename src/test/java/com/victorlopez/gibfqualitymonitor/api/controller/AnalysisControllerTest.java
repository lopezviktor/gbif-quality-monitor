package com.victorlopez.gibfqualitymonitor.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.victorlopez.gibfqualitymonitor.api.dto.AnalysisRequestDTO;
import com.victorlopez.gibfqualitymonitor.api.mapper.AnalysisReportMapper;
import com.victorlopez.gibfqualitymonitor.application.usecase.FindAnalysisReportsUseCase;
import com.victorlopez.gibfqualitymonitor.application.usecase.RequestAnalysisUseCase;
import com.victorlopez.gibfqualitymonitor.config.JacksonConfig;
import com.victorlopez.gibfqualitymonitor.domain.model.AnalysisReport;
import com.victorlopez.gibfqualitymonitor.gbif.client.GbifApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
@Import({AnalysisReportMapper.class, JacksonConfig.class})
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RequestAnalysisUseCase requestAnalysisUseCase;

    @MockitoBean
    private FindAnalysisReportsUseCase findAnalysisReportsUseCase;

    private AnalysisReport sampleReport;
    private final UUID reportId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sampleReport = AnalysisReport.builder()
                .id(reportId)
                .taxonKey(1234567L)
                .scientificName("Panthera leo")
                .country("ZA")
                .requestedLimit(100)
                .recordsAnalyzed(80)
                .returnedByGbif(80)
                .completenessScore(72.5)
                .scoreGrade("B")
                .metrics(Map.of("coordinatesCoverage", 90.0))
                .scoreBreakdown(Map.of("geographicScore", 30.0))
                .recommendations(List.of("Suitable for basic geographic exploration"))
                .createdAt(LocalDateTime.of(2025, 6, 1, 12, 0))
                .build();
    }

    // ── POST /api/v1/analyses ─────────────────────────────────────────────────

    @Test
    void post_returnsCreatedWithReportDTO() throws Exception {
        when(requestAnalysisUseCase.execute(1234567L, 100, "ZA")).thenReturn(sampleReport);

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setTaxonKey(1234567L);
        request.setLimit(100);
        request.setCountry("ZA");

        mockMvc.perform(post("/api/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.taxonKey").value(1234567))
                .andExpect(jsonPath("$.scientificName").value("Panthera leo"))
                .andExpect(jsonPath("$.scoreGrade").value("B"));
    }

    @Test
    void post_withNullTaxonKey_returnsBadRequest() throws Exception {
        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setLimit(100);

        mockMvc.perform(post("/api/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_withNullLimit_returnsBadRequest() throws Exception {
        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setTaxonKey(1234567L);

        mockMvc.perform(post("/api/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_withLimitAboveMax_returnsBadRequest() throws Exception {
        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setTaxonKey(1234567L);
        request.setLimit(501);

        mockMvc.perform(post("/api/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_whenGbifApiExceptionThrown_returns502() throws Exception {
        when(requestAnalysisUseCase.execute(any(), any(), any()))
                .thenThrow(new GbifApiException("upstream error"));

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setTaxonKey(1234567L);
        request.setLimit(100);

        mockMvc.perform(post("/api/v1/analyses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway());
    }

    // ── GET /api/v1/analyses ──────────────────────────────────────────────────

    @Test
    void getAll_returnsOkWithSummaryList() throws Exception {
        when(findAnalysisReportsUseCase.findAll()).thenReturn(List.of(sampleReport));

        mockMvc.perform(get("/api/v1/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reportId").value(reportId.toString()))
                .andExpect(jsonPath("$[0].scientificName").value("Panthera leo"))
                .andExpect(jsonPath("$[0].scoreGrade").value("B"));
    }

    @Test
    void getAll_whenEmpty_returnsEmptyList() throws Exception {
        when(findAnalysisReportsUseCase.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/v1/analyses/{id} ─────────────────────────────────────────────

    @Test
    void getById_whenFound_returnsOkWithDTO() throws Exception {
        when(findAnalysisReportsUseCase.findById(reportId)).thenReturn(Optional.of(sampleReport));

        mockMvc.perform(get("/api/v1/analyses/{id}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").value(reportId.toString()))
                .andExpect(jsonPath("$.completenessScore").value(72.5));
    }

    @Test
    void getById_whenNotFound_returns404() throws Exception {
        when(findAnalysisReportsUseCase.findById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/analyses/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
