package io.github.blueprintplatform.samples.transportcoverage.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TransportControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void multipartUploadBindsFileAndJsonPart() throws Exception {
        var file = new MockMultipartFile("file", "contract.txt", "text/plain", "hello".getBytes());
        var metadata =
                new MockMultipartFile(
                        "metadata",
                        "",
                        MediaType.APPLICATION_JSON_VALUE,
                        "{\"description\":\"multipart regression\",\"category\":\"contract\"}".getBytes());

        mockMvc
                .perform(multipart("/transport/multipart").file(file).file(metadata))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("contract.txt"))
                .andExpect(jsonPath("$.data.description").value("multipart regression"));
    }

    @Test
    void binaryDownloadReturnsOctetStream() throws Exception {
        mockMvc
                .perform(get("/transport/binary/sample"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes("transport-coverage:sample".getBytes()));
    }

    @Test
    void formUrlEncodedRequestReturnsGenericResponse() throws Exception {
        mockMvc
                .perform(
                        post("/transport/form")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "blueprint")
                                .param("category", "platform"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("blueprint"))
                .andExpect(jsonPath("$.data.category").value("platform"));
    }
}
