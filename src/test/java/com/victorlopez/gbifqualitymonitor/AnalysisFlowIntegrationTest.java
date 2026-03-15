package com.victorlopez.gbifqualitymonitor;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.victorlopez.gbifqualitymonitor.api.dto.AnalysisRequestDTO;
import com.victorlopez.gbifqualitymonitor.infrastructure.persistence.repository.AnalysisReportRepository;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AnalysisFlowIntegrationTest {

    private static final String OCCURRENCE_SEARCH_PATH = "/occurrence/search";
    private static final String FIXTURE_PATH = "wiremock/gbif-occurrences-response.json";

    // Static initializer ensures WireMock is started before Spring loads the context,
    // which is required so @DynamicPropertySource can resolve the port.
    private static final WireMockServer wireMockServer;

    static {
        wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @DynamicPropertySource
    static void overrideGbifBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("gbif.api.base-url", wireMockServer::baseUrl);
    }

    @LocalServerPort
    private int port;

    // Never throw on 4xx/5xx — let assertions inspect the status code directly.
    private final RestTemplate restTemplate = new RestTemplate() {{
        setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.HttpStatusCode statusCode) {
                return false;
            }
        });
    }};

    @Autowired
    private AnalysisReportRepository repository;

    @BeforeEach
    void resetState() {
        wireMockServer.resetAll();
        repository.deleteAll();
    }

    private String analysesUrl() {
        return "http://localhost:" + port + "/api/v1/analyses";
    }

    private String loadFixture() throws IOException {
        try (var stream = getClass().getClassLoader().getResourceAsStream(FIXTURE_PATH)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    // ── Test 1: happy path ────────────────────────────────────────────────────

    @Test
    @SuppressWarnings("unchecked")
    void happyPath_returnsCreatedReportAndPersistsIt() throws IOException {
        wireMockServer.stubFor(get(urlPathEqualTo(OCCURRENCE_SEARCH_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(loadFixture())));

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setTaxonKey(5219173L);
        request.setLimit(5);
        request.setCountry("ES");

        ResponseEntity<Map> response = restTemplate.postForEntity(analysesUrl(), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();

        assertThat((Double) body.get("completenessScore"))
                .isNotNull()
                .isGreaterThanOrEqualTo(0.0);

        assertThat((String) body.get("scoreGrade")).isNotNull();

        assertThat((Integer) body.get("recordsAnalyzed")).isEqualTo(5);

        assertThat(repository.count()).isEqualTo(1);
    }

    // ── Test 2: GBIF unavailable → 502 ────────────────────────────────────────

    @Test
    void gbifUnavailable_returns502BadGateway() {
        wireMockServer.stubFor(get(urlPathEqualTo(OCCURRENCE_SEARCH_PATH))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        AnalysisRequestDTO request = new AnalysisRequestDTO();
        request.setTaxonKey(5219173L);
        request.setLimit(5);

        ResponseEntity<String> response = restTemplate.postForEntity(analysesUrl(), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
    }
}
