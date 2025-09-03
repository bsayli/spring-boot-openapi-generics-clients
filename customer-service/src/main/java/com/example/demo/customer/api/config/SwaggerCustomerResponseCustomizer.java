package com.example.demo.customer.api.config;

import static com.example.demo.common.api.config.OpenApiSchemas.SCHEMA_API_RESPONSE;

import com.example.demo.common.api.config.OpenApiSchemaUtils;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerCustomerResponseCustomizer {

  private static final String REF_CUSTOMER_DTO              = "CustomerDto";
  private static final String REF_CUSTOMER_CREATE_RESPONSE  = "CustomerCreateResponse";
  private static final String REF_CUSTOMER_UPDATE_RESPONSE  = "CustomerUpdateResponse";
  private static final String REF_CUSTOMER_DELETE_RESPONSE  = "CustomerDeleteResponse";
  private static final String REF_CUSTOMER_LIST_RESPONSE    = "CustomerListResponse";

  private static final List<String> REGISTERED_RESPONSE_REFS = List.of(
          REF_CUSTOMER_DTO,
          REF_CUSTOMER_CREATE_RESPONSE,
          REF_CUSTOMER_UPDATE_RESPONSE,
          REF_CUSTOMER_DELETE_RESPONSE,
          REF_CUSTOMER_LIST_RESPONSE
  );

  private static String apiResponseWrapperNameFor(String ref) {
    return SCHEMA_API_RESPONSE + ref; // örn: ApiResponseCustomerDto
  }

  @Bean
  public OpenApiCustomizer customerWrappers() {
    return openApi -> REGISTERED_RESPONSE_REFS.forEach(ref ->
            openApi.getComponents().addSchemas(
                    apiResponseWrapperNameFor(ref),
                    OpenApiSchemaUtils.createComposedWrapper(ref) // allOf(ApiResponse + {data:$ref})
            )
    );
  }
}