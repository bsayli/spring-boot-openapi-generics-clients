package io.github.bsayli.customerservice.api.dto;

import java.time.Instant;

public record CustomerUpdateResponse(CustomerDto customer, Instant updatedAt) {}
