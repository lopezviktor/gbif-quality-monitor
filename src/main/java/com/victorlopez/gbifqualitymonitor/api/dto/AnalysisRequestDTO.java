package com.victorlopez.gbifqualitymonitor.api.dto;

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

    @NotNull
    private Long taxonKey;

    @NotNull
    @Min(1)
    @Max(500)
    private Integer limit;

    private String country;
}
