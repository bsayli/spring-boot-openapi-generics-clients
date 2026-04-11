package io.github.blueprintplatform.samples.customerservice.api.dto;

import org.springdoc.core.annotations.ParameterObject;

@ParameterObject
public record CustomerSearchCriteria(String name, String email) {}
