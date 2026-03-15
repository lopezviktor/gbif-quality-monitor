package com.victorlopez.gbifqualitymonitor.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnalysisRequestDTO {

    @Schema(description = "GBIF taxon key identifier", example = "5219173")
    @NotNull
    private Long taxonKey;

    @Schema(description = "Maximum number of records to analyse (1–500)", example = "100")
    @NotNull
    @Min(1)
    @Max(500)
    private Integer limit;

    @Schema(description = "ISO 3166-1 alpha-2 country code (optional)", example = "ES")
    private String country;
}
