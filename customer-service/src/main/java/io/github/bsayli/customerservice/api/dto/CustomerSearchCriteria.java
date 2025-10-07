package io.github.bsayli.customerservice.api.dto;

import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
public record CustomerSearchCriteria(String name, String email) {}
