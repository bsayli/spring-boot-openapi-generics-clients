package io.github.bsayli.customerservice.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import org.junit.jupiter.api.*;

@Tag("unit")
@DisplayName("DTO JSON: CustomerCreateResponse")
class CustomerCreateResponseJsonTest {

  private ObjectMapper om;

  @BeforeEach
  void setUp() {
    om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  @Test
  @DisplayName("createdAt should serialize as ISO-8601 string")
  void createdAt_shouldBeIso8601() throws Exception {
    var customer = new CustomerDto(10, "Jane", "jane@example.com");
    var resp = new CustomerCreateResponse(customer, Instant.parse("2025-01-01T12:34:56Z"));

    String json = om.writeValueAsString(resp);
    assertThat(json).contains("\"createdAt\":\"2025-01-01T12:34:56Z\"");
  }
}
