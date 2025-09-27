package io.github.bsayli.customerservice.api.dto;

import java.time.Instant;

public record CustomerDeleteResponse(Integer customerId, Instant deletedAt) {}
