package io.github.blueprintplatform.samples.typecoverage.openapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiSnapshotGenerationTest {

    private static final String API_DOCS_YAML = "/v3/api-docs.yaml";

    private static final Path OUTPUT =
            Path.of("..", "spec", "service-response-coverage.yaml");

    private static final String OPENAPI_MARKER = "openapi:";

    private static final String API_WRAPPER_EXTENSION = "x-api-wrapper";
    private static final String API_WRAPPER_DATATYPE_EXTENSION = "x-api-wrapper-datatype";
    private static final String DATA_CONTAINER_EXTENSION = "x-data-container";
    private static final String DATA_ITEM_EXTENSION = "x-data-item";
    private static final String IGNORE_MODEL_EXTENSION = "x-ignore-model";

    private static final String SIMPLE_WRAPPER_SCHEMA = "ServiceResponseString";
    private static final String DTO_WRAPPER_SCHEMA = "ServiceResponseTypeProfileDto";
    private static final String ENUM_WRAPPER_SCHEMA = "ServiceResponseCoverageStatus";
    private static final String LIST_DTO_WRAPPER_SCHEMA = "ServiceResponseListTypeSummaryDto";
    private static final String LIST_ENUM_WRAPPER_SCHEMA = "ServiceResponseListCoverageStatus";
    private static final String PAGE_DTO_WRAPPER_SCHEMA = "ServiceResponsePageTypeSummaryDto";
    private static final String PAGE_ENUM_WRAPPER_SCHEMA = "ServiceResponsePageCoverageStatus";

    private static final String LIST_CONTAINER_MARKER = "x-data-container: List";
    private static final String PAGE_CONTAINER_MARKER = "x-data-container: Page";
    private static final String TYPE_SUMMARY_ITEM_MARKER = "x-data-item: TypeSummaryDto";
    private static final String COVERAGE_STATUS_ITEM_MARKER = "x-data-item: CoverageStatus";

    private static final String ASYNC_DIRECT_PATH = "/types/async/summary:";
    private static final String ASYNC_PAGE_PATH = "/types/async/paged-summaries:";
    private static final String ASYNC_DIRECT_OPERATION = "operationId: completionStageSummary";
    private static final String ASYNC_PAGE_OPERATION = "operationId: deferredPagedSummaries";
    private static final String ASYNC_CONTROLLER_TAG = "async-payload-controller";

    @Autowired private MockMvc mockMvc;

    @Test
    void generateOpenApiYamlSnapshot() throws Exception {
        String yaml = fetchOpenApiYaml();

        assertThat(yaml)
                .contains(
                        OPENAPI_MARKER,
                        API_WRAPPER_EXTENSION,
                        API_WRAPPER_DATATYPE_EXTENSION,
                        DATA_CONTAINER_EXTENSION,
                        DATA_ITEM_EXTENSION,
                        IGNORE_MODEL_EXTENSION,
                        SIMPLE_WRAPPER_SCHEMA,
                        DTO_WRAPPER_SCHEMA,
                        ENUM_WRAPPER_SCHEMA,
                        LIST_DTO_WRAPPER_SCHEMA,
                        LIST_ENUM_WRAPPER_SCHEMA,
                        PAGE_DTO_WRAPPER_SCHEMA,
                        PAGE_ENUM_WRAPPER_SCHEMA,
                        LIST_CONTAINER_MARKER,
                        PAGE_CONTAINER_MARKER,
                        TYPE_SUMMARY_ITEM_MARKER,
                        COVERAGE_STATUS_ITEM_MARKER,
                        ASYNC_DIRECT_PATH,
                        ASYNC_PAGE_PATH,
                        ASYNC_DIRECT_OPERATION,
                        ASYNC_PAGE_OPERATION,
                        ASYNC_CONTROLLER_TAG);

        writeSnapshot(yaml);
    }

    private String fetchOpenApiYaml() throws Exception {
        return mockMvc
                .perform(get(API_DOCS_YAML))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private void writeSnapshot(String yaml) throws Exception {
        Files.createDirectories(OUTPUT.getParent());
        Files.writeString(OUTPUT, yaml, StandardCharsets.UTF_8);
    }
}
