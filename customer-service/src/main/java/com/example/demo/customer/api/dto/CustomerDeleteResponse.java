package com.example.demo.customer.api.dto;

import java.time.Instant;

public record CustomerDeleteResponse(Integer customerId, Instant deletedAt) {}
