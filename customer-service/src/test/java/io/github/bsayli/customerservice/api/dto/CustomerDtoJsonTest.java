package io.github.bsayli.customerservice.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;

@Tag("unit")
@DisplayName("DTO JSON: CustomerDto")
class CustomerDtoJsonTest {

  private ObjectMapper om;

  @BeforeEach
  void setUp() {
    om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
  }

  @Test
  @DisplayName("serialize/deserialize should preserve fields")
  void roundTrip_shouldPreserveFields() throws Exception {
    var dto = new CustomerDto(5, "Mark Lee", "mark.lee@example.com");

    String json = om.writeValueAsString(dto);
    CustomerDto back = om.readValue(json, CustomerDto.class);

    assertThat(back.customerId()).isEqualTo(5);
    assertThat(back.name()).isEqualTo("Mark Lee");
    assertThat(back.email()).isEqualTo("mark.lee@example.com");
  }
}
