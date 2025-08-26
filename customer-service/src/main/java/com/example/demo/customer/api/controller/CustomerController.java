package com.example.demo.customer.api.controller;

import com.example.demo.common.api.response.ApiResponse;
import com.example.demo.customer.api.dto.CustomerCreateRequest;
import com.example.demo.customer.api.dto.CustomerCreateResponse;
import com.example.demo.customer.api.dto.CustomerDto;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/customers")
@Validated
public class CustomerController {

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerCreateResponse>> create(
            @Valid @RequestBody CustomerCreateRequest request) {

        var customer = new CustomerDto(1L, request.name(), request.email()); // demo-only
        var response = new CustomerCreateResponse(customer, Instant.now());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(HttpStatus.CREATED, "CREATED", response));
    }
}