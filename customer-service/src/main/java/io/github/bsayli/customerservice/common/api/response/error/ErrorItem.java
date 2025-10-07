package io.github.bsayli.customerservice.common.api.response.error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorItem(String code, String message, String field, String resource, String id) {}
