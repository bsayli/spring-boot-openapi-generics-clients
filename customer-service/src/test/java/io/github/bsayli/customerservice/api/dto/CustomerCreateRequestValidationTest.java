package io.github.bsayli.customerservice.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

@Tag("unit")
@DisplayName("DTO Validation: CustomerCreateRequest")
class CustomerCreateRequestValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setup() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  @DisplayName("name/email valid -> no violations")
  void validPayload_shouldPass() {
    var dto = new CustomerCreateRequest("John Smith", "john.smith@example.com");
    assertThat(validator.validate(dto)).isEmpty();
  }

  @Test
  @DisplayName("blank name -> validation fails")
  void blankName_shouldFail() {
    var dto = new CustomerCreateRequest("  ", "john.smith@example.com");
    assertThat(validator.validate(dto)).isNotEmpty();
  }

  @Test
  @DisplayName("invalid email -> validation fails")
  void invalidEmail_shouldFail() {
    var dto = new CustomerCreateRequest("John Smith", "not-an-email");
    assertThat(validator.validate(dto)).isNotEmpty();
  }
}
