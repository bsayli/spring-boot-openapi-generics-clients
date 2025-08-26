package com.example.demo.client.adapter.impl;

import com.example.demo.client.adapter.CustomerClientAdapter;
import com.example.demo.client.common.ApiClientResponse;
import com.example.demo.client.generated.api.CustomerControllerApi;
import com.example.demo.client.generated.dto.CustomerCreateRequest;
import com.example.demo.client.generated.dto.CustomerCreateResponse;
import org.springframework.stereotype.Service;

@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

    private final CustomerControllerApi customerControllerApi;

    public CustomerClientAdapterImpl(CustomerControllerApi customerControllerApi) {
        this.customerControllerApi = customerControllerApi;
    }

    @Override
    public ApiClientResponse<CustomerCreateResponse> create(CustomerCreateRequest request) {
        return customerControllerApi.create(request);
    }
}