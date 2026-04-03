package io.github.bsayli.customerservice.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

@Tag("unit")
@DisplayName("DTO Validation: CustomerUpdateRequest")
class CustomerUpdateRequestValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setup() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  @DisplayName("valid update -> no violations")
  void validUpdate_shouldPass() {
    var dto = new CustomerUpdateRequest("Jane Doe", "jane.doe@example.com");
    assertThat(validator.validate(dto)).isEmpty();
  }

  @Test
  @DisplayName("blank name or invalid email -> violations")
  void invalidUpdate_shouldFail() {
    var dto1 = new CustomerUpdateRequest("", "jane.doe@example.com");
    var dto2 = new CustomerUpdateRequest("Jane Doe", "bad-email");
    assertThat(validator.validate(dto1)).isNotEmpty();
    assertThat(validator.validate(dto2)).isNotEmpty();
  }
}
