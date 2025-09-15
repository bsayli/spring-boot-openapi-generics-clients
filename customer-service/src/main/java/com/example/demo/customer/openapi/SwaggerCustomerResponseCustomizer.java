package com.example.demo.customer.openapi;

import static com.example.demo.common.openapi.OpenApiSchemas.SCHEMA_SERVICE_RESPONSE;

import com.example.demo.common.openapi.ApiResponseSchemaFactory;
import com.example.demo.customer.api.dto.CustomerCreateResponse;
import com.example.demo.customer.api.dto.CustomerDeleteResponse;
import com.example.demo.customer.api.dto.CustomerDto;
import com.example.demo.customer.api.dto.CustomerListResponse;
import com.example.demo.customer.api.dto.CustomerUpdateResponse;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerCustomerResponseCustomizer {

  private static final String REF_CUSTOMER_DTO = CustomerDto.class.getSimpleName();
  private static final String REF_CUSTOMER_CREATE_RESPONSE =
      CustomerCreateResponse.class.getSimpleName();
  private static final String REF_CUSTOMER_UPDATE_RESPONSE =
      CustomerUpdateResponse.class.getSimpleName();
  private static final String REF_CUSTOMER_DELETE_RESPONSE =
      CustomerDeleteResponse.class.getSimpleName();
  private static final String REF_CUSTOMER_LIST_RESPONSE =
      CustomerListResponse.class.getSimpleName();

  private static final List<String> REGISTERED_RESPONSE_REFS =
      List.of(
          REF_CUSTOMER_DTO,
          REF_CUSTOMER_CREATE_RESPONSE,
          REF_CUSTOMER_UPDATE_RESPONSE,
          REF_CUSTOMER_DELETE_RESPONSE,
          REF_CUSTOMER_LIST_RESPONSE);

  private static String apiResponseWrapperNameFor(String ref) {
    return SCHEMA_SERVICE_RESPONSE + ref; // e.g. ServiceResponseCustomerDto
  }

  @Bean
  public OpenApiCustomizer customerWrappers() {
    return openApi ->
        REGISTERED_RESPONSE_REFS.forEach(
            ref ->
                openApi
                    .getComponents()
                    .addSchemas(
                        apiResponseWrapperNameFor(ref),
                        ApiResponseSchemaFactory.createComposedWrapper(ref)));
  }
}
