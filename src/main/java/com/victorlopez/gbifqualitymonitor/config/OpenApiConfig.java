package com.victorlopez.gbifqualitymonitor.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GBIF Data Quality Monitor",
                version = "1.0.0",
                description = "REST API for assessing the fitness-for-use of biodiversity occurrence data from GBIF",
                contact = @Contact(
                        name = "Victor Lopez",
                        url = "https://github.com/lopezviktor"
                )
        )
)
public class OpenApiConfig {
}
