package com.example.demo.customer.api.dto;

import java.time.Instant;

public record CustomerUpdateResponse(CustomerDto customer, Instant updatedAt) {}
