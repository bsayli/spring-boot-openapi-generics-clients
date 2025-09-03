package com.example.demo.customer.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerUpdateRequest(
    @NotBlank @Size(min = 2, max = 80) String name, @NotBlank @Email String email) {}
