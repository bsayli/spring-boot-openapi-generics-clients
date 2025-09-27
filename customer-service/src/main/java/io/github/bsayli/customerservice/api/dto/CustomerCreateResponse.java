package io.github.bsayli.customerservice.api.dto;

import java.time.Instant;

public record CustomerCreateResponse(CustomerDto customer, Instant createdAt) {}
