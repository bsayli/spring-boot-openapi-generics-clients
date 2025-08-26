package com.example.demo.customer.api.dto;

import java.time.Instant;

public record CustomerCreateResponse(
        CustomerDto customer,
        Instant createdAt
) {}