package io.github.bsayli.openapi.client.adapter.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.github.bsayli.openapi.client.adapter.CustomerClientAdapter;
import io.github.bsayli.openapi.client.common.ClientMeta;
import io.github.bsayli.openapi.client.common.ServiceClientResponse;
import io.github.bsayli.openapi.client.generated.api.CustomerControllerApi;
import io.github.bsayli.openapi.client.generated.dto.CustomerCreateRequest;
import io.github.bsayli.openapi.client.generated.dto.CustomerDeleteResponse;
import io.github.bsayli.openapi.client.generated.dto.CustomerDto;
import io.github.bsayli.openapi.client.generated.dto.CustomerUpdateRequest;
import io.github.bsayli.openapi.client.generated.dto.PageCustomerDto;
import io.github.bsayli.openapi.client.generated.dto.ServiceResponseCustomerDeleteResponse;
import io.github.bsayli.openapi.client.generated.dto.ServiceResponseCustomerDto;
import io.github.bsayli.openapi.client.generated.dto.ServiceResponsePageCustomerDto;
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
@DisplayName("Unit Test: CustomerClientAdapterImpl (data + meta mapping)")
class CustomerClientAdapterImplTest {

  @Mock CustomerControllerApi api;

  @InjectMocks CustomerClientAdapterImpl adapter;

  @Test
  @DisplayName(
      "createCustomer -> delegates to API and returns ServiceClientResponse<CustomerDto> (including meta)")
  void createCustomer_delegates_and_returns_data_meta() {
    var req = new CustomerCreateRequest().name("Jane Doe").email("jane@example.com");

    var dto = new CustomerDto().customerId(1).name("Jane Doe").email("jane@example.com");

    var serverOdt = OffsetDateTime.parse("2025-01-01T12:34:56Z");

    var meta = new ClientMeta("req-123", serverOdt.toInstant(), List.of());

    var wrapper = new ServiceResponseCustomerDto();
    wrapper.setData(dto);
    wrapper.setMeta(meta);

    when(api.createCustomer(any(CustomerCreateRequest.class))).thenReturn(wrapper);

    ServiceClientResponse<CustomerDto> res = adapter.createCustomer(req);

    assertNotNull(res);
    assertNotNull(res.getData());
    assertEquals(1, res.getData().getCustomerId());
    assertEquals("Jane Doe", res.getData().getName());
    assertEquals("jane@example.com", res.getData().getEmail());

    assertNotNull(res.getMeta());
    assertEquals("req-123", res.getMeta().getRequestId());
    assertEquals(serverOdt.toInstant(), res.getMeta().getServerTime());
  }

  @Test
  @DisplayName("getCustomer -> returns a single CustomerDto (data + meta)")
  void getCustomer_delegates_and_returnsDto() {
    var dto = new CustomerDto().customerId(42).name("John Smith").email("john.smith@example.com");

    var serverOdt = OffsetDateTime.parse("2025-02-01T10:00:00Z");

    var wrapper = new ServiceResponseCustomerDto();
    wrapper.setData(dto);
    wrapper.setMeta(new ClientMeta("req-42", serverOdt.toInstant(), List.of()));

    when(api.getCustomer(any())).thenReturn(wrapper);

    ServiceClientResponse<CustomerDto> res = adapter.getCustomer(42);

    assertNotNull(res);
    assertNotNull(res.getData());
    assertEquals(42, res.getData().getCustomerId());
    assertEquals("John Smith", res.getData().getName());
    assertEquals("john.smith@example.com", res.getData().getEmail());

    assertNotNull(res.getMeta());
    assertEquals("req-42", res.getMeta().getRequestId());
    assertEquals(serverOdt.toInstant(), res.getMeta().getServerTime());
  }

  @Test
  @DisplayName("getCustomers -> returns Page<CustomerDto> (data + meta)")
  void getCustomers_delegates_and_returnsPage() {
    var d1 = new CustomerDto().customerId(1).name("A").email("a@example.com");
    var d2 = new CustomerDto().customerId(2).name("B").email("b@example.com");

    var pageDto =
        new PageCustomerDto()
            .content(List.of(d1, d2))
            .page(0)
            .size(5)
            .totalElements(2L)
            .totalPages(1)
            .hasNext(false)
            .hasPrev(false);

    var serverOdt = OffsetDateTime.parse("2025-03-01T09:00:00Z");

    var wrapper = new ServiceResponsePageCustomerDto();
    wrapper.setData(pageDto);
    wrapper.setMeta(new ClientMeta("req-list", serverOdt.toInstant(), List.of()));

    when(api.getCustomers(any(), any(), any(), any(), any(), any())).thenReturn(wrapper);

    ServiceClientResponse<PageCustomerDto> res =
        adapter.getCustomers(null, null, 0, 5, "customerId", "asc");

    assertNotNull(res);
    assertNotNull(res.getData());
    assertEquals(0, res.getData().getPage());
    assertEquals(5, res.getData().getSize());
    assertEquals(2L, res.getData().getTotalElements());
    assertNotNull(res.getData().getContent());
    assertEquals(2, res.getData().getContent().size());
    assertEquals(1, res.getData().getContent().getFirst().getCustomerId());

    assertNotNull(res.getMeta());
    assertEquals("req-list", res.getMeta().getRequestId());
    assertEquals(serverOdt.toInstant(), res.getMeta().getServerTime());
  }

  @Test
  @DisplayName("updateCustomer -> returns updated CustomerDto (data + meta)")
  void updateCustomer_delegates_and_returnsUpdated() {
    var req = new CustomerUpdateRequest().name("Jane Updated").email("jane.updated@example.com");

    var dto =
        new CustomerDto().customerId(1).name("Jane Updated").email("jane.updated@example.com");

    var serverOdt = OffsetDateTime.parse("2025-04-02T12:00:00Z");

    var wrapper = new ServiceResponseCustomerDto();
    wrapper.setData(dto);
    wrapper.setMeta(new ClientMeta("req-upd", serverOdt.toInstant(), List.of()));

    when(api.updateCustomer(any(), any(CustomerUpdateRequest.class))).thenReturn(wrapper);

    ServiceClientResponse<CustomerDto> res = adapter.updateCustomer(1, req);

    assertNotNull(res);
    assertNotNull(res.getData());
    assertEquals("Jane Updated", res.getData().getName());
    assertEquals("jane.updated@example.com", res.getData().getEmail());

    assertNotNull(res.getMeta());
    assertEquals("req-upd", res.getMeta().getRequestId());
    assertEquals(serverOdt.toInstant(), res.getMeta().getServerTime());
  }

  @Test
  @DisplayName("deleteCustomer -> returns CustomerDeleteResponse (data + meta)")
  void deleteCustomer_delegates_and_returnsDeletePayload() {
    var payload = new CustomerDeleteResponse().customerId(7);

    var serverOdt = OffsetDateTime.parse("2025-05-03T08:00:00Z");

    var wrapper = new ServiceResponseCustomerDeleteResponse();
    wrapper.setData(payload);
    wrapper.setMeta(new ClientMeta("req-del", serverOdt.toInstant(), List.of()));

    when(api.deleteCustomer(any())).thenReturn(wrapper);

    ServiceClientResponse<CustomerDeleteResponse> res = adapter.deleteCustomer(7);

    assertNotNull(res);
    assertNotNull(res.getData());
    assertEquals(7, res.getData().getCustomerId());

    assertNotNull(res.getMeta());
    assertEquals("req-del", res.getMeta().getRequestId());
    assertEquals(serverOdt.toInstant(), res.getMeta().getServerTime());
  }

  @Test
  @DisplayName("Adapter interface type check")
  void adapter_type_sanity() {
    CustomerClientAdapter asInterface = adapter;
    assertNotNull(asInterface);
  }
}
