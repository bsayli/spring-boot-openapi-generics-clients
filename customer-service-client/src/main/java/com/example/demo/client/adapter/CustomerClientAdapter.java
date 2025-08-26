package com.example.demo.client.adapter;

import com.example.demo.client.common.ApiClientResponse;
import com.example.demo.client.generated.dto.CustomerCreateRequest;
import com.example.demo.client.generated.dto.CustomerCreateResponse;

public interface CustomerClientAdapter {
    ApiClientResponse<CustomerCreateResponse> create(CustomerCreateRequest request);
}