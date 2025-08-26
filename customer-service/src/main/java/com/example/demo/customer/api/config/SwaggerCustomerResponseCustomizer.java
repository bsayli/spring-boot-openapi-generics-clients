package com.example.demo.customer.api.config;

import com.example.demo.common.api.config.OpenApiSchemaUtils;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static com.example.demo.common.api.config.OpenApiSchemas.SCHEMA_API_RESPONSE;

@Configuration
public class SwaggerCustomerResponseCustomizer {

    private static final String REF_CUSTOMER_CREATE_RESPONSE = "CustomerCreateResponse";

    // Add new response DTO refs here (e.g., "CustomerUpdateResponse", "CustomerGetResponse", ...)
    private static final List<String> REGISTERED_RESPONSE_REFS = List.of(
            REF_CUSTOMER_CREATE_RESPONSE
    );

    private static String apiResponseWrapperNameFor(String ref) {
        return SCHEMA_API_RESPONSE + ref;
    }

    @Bean
    public OpenApiCustomizer customerWrappers() {
        return openApi -> REGISTERED_RESPONSE_REFS.forEach(ref ->
                openApi.getComponents().addSchemas(
                        apiResponseWrapperNameFor(ref),
                        OpenApiSchemaUtils.createComposedWrapper(ref)
                )
        );
    }
}