package io.github.bsayli.openapi.client.adapter;

import io.github.bsayli.openapi.client.common.ApiClientResponse;
import io.github.bsayli.openapi.client.generated.dto.CustomerCreateRequest;
import io.github.bsayli.openapi.client.generated.dto.CustomerCreateResponse;

public interface CustomerClientAdapter {
    ApiClientResponse<CustomerCreateResponse> create(CustomerCreateRequest request);
}