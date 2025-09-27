package io.github.bsayli.openapi.client.adapter.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.bsayli.openapi.client.adapter.CustomerClientAdapter;
import io.github.bsayli.openapi.client.common.ServiceClientResponse;
import io.github.bsayli.openapi.client.generated.api.CustomerControllerApi;
import io.github.bsayli.openapi.client.generated.dto.CustomerCreateRequest;
import io.github.bsayli.openapi.client.generated.dto.CustomerCreateResponse;
import io.github.bsayli.openapi.client.generated.dto.CustomerDeleteResponse;
import io.github.bsayli.openapi.client.generated.dto.CustomerDto;
import io.github.bsayli.openapi.client.generated.dto.CustomerListResponse;
import io.github.bsayli.openapi.client.generated.dto.CustomerUpdateRequest;
import io.github.bsayli.openapi.client.generated.dto.CustomerUpdateResponse;
import io.github.bsayli.openapi.client.generated.dto.ServiceResponseCustomerCreateResponse;
import io.github.bsayli.openapi.client.generated.dto.ServiceResponseCustomerDeleteResponse;
import io.github.bsayli.openapi.client.generated.dto.ServiceResponseCustomerDto;
import io.github.bsayli.openapi.client.generated.dto.ServiceResponseCustomerListResponse;
import io.github.bsayli.openapi.client.generated.dto.ServiceResponseCustomerUpdateResponse;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Test: CustomerClientAdapterImpl")
class CustomerClientAdapterImplTest {

  @Mock CustomerControllerApi api;

  @InjectMocks CustomerClientAdapterImpl adapter;

  @Test
  @DisplayName("createCustomer -> delegates to API and returns 201 + payload passthrough")
  void createCustomer_delegates_and_passthrough() {
    var req = new CustomerCreateRequest().name("Jane Doe").email("jane@example.com");

    var dto = new CustomerDto().customerId(1).name("Jane Doe").email("jane@example.com");

    var payload =
        new CustomerCreateResponse()
            .customer(dto)
            .createdAt(OffsetDateTime.parse("2025-01-01T12:34:56Z"));

    var wrapper = new ServiceResponseCustomerCreateResponse();
    wrapper.setStatus(201);
    wrapper.setMessage("CREATED");
    wrapper.setErrors(List.of());
    wrapper.setData(payload);

    when(api.createCustomer(any(CustomerCreateRequest.class))).thenReturn(wrapper);

    ServiceClientResponse<CustomerCreateResponse> res = adapter.createCustomer(req);

    assertNotNull(res);
    assertEquals(201, res.getStatus());
    assertEquals("CREATED", res.getMessage());
    assertNotNull(res.getData());
    assertNotNull(res.getData().getCustomer());
    assertEquals(1, res.getData().getCustomer().getCustomerId());
    assertEquals(OffsetDateTime.parse("2025-01-01T12:34:56Z"), res.getData().getCreatedAt());
  }

  @Test
  @DisplayName("getCustomer -> delegates to API and returns typed DTO")
  void getCustomer_delegates_and_returnsDto() {
    var dto = new CustomerDto().customerId(42).name("John Smith").email("john.smith@example.com");

    var wrapper = new ServiceResponseCustomerDto();
    wrapper.setStatus(200);
    wrapper.setMessage("OK");
    wrapper.setErrors(List.of());
    wrapper.setData(dto);

    when(api.getCustomer(42)).thenReturn(wrapper);

    ServiceClientResponse<CustomerDto> res = adapter.getCustomer(42);

    assertNotNull(res);
    assertEquals(200, res.getStatus());
    assertEquals("OK", res.getMessage());
    assertNotNull(res.getData());
    assertEquals(42, res.getData().getCustomerId());
    assertEquals("John Smith", res.getData().getName());
  }

  @Test
  @DisplayName("getCustomers -> delegates to API and returns list")
  void getCustomers_delegates_and_returnsList() {
    var d1 = new CustomerDto().customerId(1).name("A").email("a@example.com");
    var d2 = new CustomerDto().customerId(2).name("B").email("b@example.com");
    var listPayload = new CustomerListResponse().customers(List.of(d1, d2));

    var wrapper = new ServiceResponseCustomerListResponse();
    wrapper.setStatus(200);
    wrapper.setMessage("LISTED");
    wrapper.setErrors(List.of());
    wrapper.setData(listPayload);

    when(api.getCustomers()).thenReturn(wrapper);

    ServiceClientResponse<CustomerListResponse> res = adapter.getCustomers();

    assertNotNull(res);
    assertEquals(200, res.getStatus());
    assertEquals("LISTED", res.getMessage());
    assertNotNull(res.getData());
    assertNotNull(res.getData().getCustomers());
    assertEquals(2, res.getData().getCustomers().size());
    assertEquals(1, res.getData().getCustomers().getFirst().getCustomerId());
  }

  @Test
  @DisplayName("updateCustomer -> delegates to API and returns UPDATED payload")
  void updateCustomer_delegates_and_returnsUpdated() {
    var req = new CustomerUpdateRequest().name("Jane Updated").email("jane.updated@example.com");

    var dto =
        new CustomerDto().customerId(1).name("Jane Updated").email("jane.updated@example.com");

    var payload =
        new CustomerUpdateResponse()
            .customer(dto)
            .updatedAt(OffsetDateTime.parse("2025-01-02T12:00:00Z"));

    var wrapper = new ServiceResponseCustomerUpdateResponse();
    wrapper.setStatus(200);
    wrapper.setMessage("UPDATED");
    wrapper.setErrors(List.of());
    wrapper.setData(payload);

    when(api.updateCustomer(1, req)).thenReturn(wrapper);

    ServiceClientResponse<CustomerUpdateResponse> res = adapter.updateCustomer(1, req);

    assertNotNull(res);
    assertEquals(200, res.getStatus());
    assertEquals("UPDATED", res.getMessage());
    assertNotNull(res.getData());
    assertEquals("Jane Updated", res.getData().getCustomer().getName());
    assertEquals(OffsetDateTime.parse("2025-01-02T12:00:00Z"), res.getData().getUpdatedAt());
  }

  @Test
  @DisplayName("deleteCustomer -> delegates to API and returns DELETED payload")
  void deleteCustomer_delegates_and_passthrough() {
    var payload =
        new CustomerDeleteResponse()
            .customerId(7)
            .deletedAt(OffsetDateTime.parse("2025-01-03T08:00:00Z"));

    var wrapper = new ServiceResponseCustomerDeleteResponse();
    wrapper.setStatus(200);
    wrapper.setMessage("DELETED");
    wrapper.setErrors(List.of());
    wrapper.setData(payload);

    when(api.deleteCustomer(7)).thenReturn(wrapper);

    ServiceClientResponse<CustomerDeleteResponse> res = adapter.deleteCustomer(7);

    assertNotNull(res);
    assertEquals(200, res.getStatus());
    assertEquals("DELETED", res.getMessage());
    assertNotNull(res.getData());
    assertEquals(7, res.getData().getCustomerId());
    assertEquals(OffsetDateTime.parse("2025-01-03T08:00:00Z"), res.getData().getDeletedAt());
  }

  @Test
  @DisplayName("Adapter is a CustomerClientAdapter and uses the generated API underneath")
  void adapter_type_sanity() {
    CustomerClientAdapter asInterface = adapter;
    assertNotNull(asInterface);
  }
}
