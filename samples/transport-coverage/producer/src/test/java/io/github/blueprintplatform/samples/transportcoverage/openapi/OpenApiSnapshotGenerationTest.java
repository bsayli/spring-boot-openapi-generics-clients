package io.github.blueprintplatform.samples.transportcoverage.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiSnapshotGenerationTest {

    private static final String API_DOCS_YAML = "/v3/api-docs.yaml";
    private static final Path OUTPUT = Path.of("..", "spec", "transport-coverage.yaml");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void generateOpenApiYamlSnapshot() throws Exception {
        String yaml =
                mockMvc
                        .perform(get(API_DOCS_YAML))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(StandardCharsets.UTF_8);

        assertThat(yaml)
                .contains(
                        "multipart/form-data",
                        "application/x-www-form-urlencoded",
                        "application/octet-stream",
                        "format: binary",
                        "x-api-wrapper: true",
                        "x-api-wrapper-type:",
                        "x-api-wrapper-datatype: UploadResultDto",
                        "x-api-wrapper-datatype: RegistrationResultDto",
                        "ServiceResponseUploadResultDto",
                        "ServiceResponseRegistrationResultDto");

        Files.createDirectories(OUTPUT.getParent());
        Files.writeString(OUTPUT, yaml, StandardCharsets.UTF_8);
    }
}
