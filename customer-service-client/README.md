# customer-service-client

Generated Java client for the demo **customer-service**, showcasing **type-safe generic responses** with OpenAPI + a
custom Mustache template (wrapping payloads in a reusable `ApiClientResponse<T>`).

This module demonstrates how to evolve OpenAPI Generator with minimal customization to support generic response
envelopes — avoiding duplicated wrappers and preserving strong typing.

---

## ✅ What You Get

* Generated code using **OpenAPI Generator** (`restclient` with Spring Framework `RestClient`).
* A reusable generic base: `io.github.bsayli.openapi.client.common.ApiClientResponse<T>`.
* Thin wrappers per endpoint (e.g. `ApiResponseCustomerCreateResponse`, `ApiResponseCustomerUpdateResponse`).
* Spring Boot configuration to auto-expose the client as beans.
* Focused integration tests using **OkHttp MockWebServer** covering all CRUD endpoints.

---

## 🚀 Quick Pipeline (3 Steps)

1. **Run the sample service**

```bash
cd customer-service
mvn spring-boot:run
# Service base URL: http://localhost:8084/customer-service
```

2. **Pull the OpenAPI spec into this module**

```bash
cd customer-service-client
curl -s http://localhost:8084/customer-service/v3/api-docs.yaml \
  -o src/main/resources/customer-api-docs.yaml
```

3. **Generate & build the client**

```bash
mvn clean install
```

Generated sources will be placed under:

```
target/generated-sources/openapi/src/gen/java/main
```

---

## 🧩 Using the Client

### Option A — Spring Configuration (recommended)

Include this module as a dependency and configure the base URL:

```java
@Configuration
public class CustomerApiClientConfig {

  @Bean
  public RestClient customerRestClient(RestClient.Builder builder,
                                       @Value("${customer.api.base-url}") String baseUrl) {
    return builder.baseUrl(baseUrl).build();
  }

  @Bean
  public io.github.bsayli.openapi.client.generated.invoker.ApiClient customerApiClient(
      RestClient customerRestClient,
      @Value("${customer.api.base-url}") String baseUrl) {
    return new io.github.bsayli.openapi.client.generated.invoker.ApiClient(customerRestClient)
        .setBasePath(baseUrl);
  }

  @Bean
  public io.github.bsayli.openapi.client.generated.api.CustomerControllerApi customerControllerApi(
      io.github.bsayli.openapi.client.generated.invoker.ApiClient apiClient) {
    return new io.github.bsayli.openapi.client.generated.api.CustomerControllerApi(apiClient);
  }
}
```

**application.properties:**

```properties
customer.api.base-url=http://localhost:8084/customer-service
```

**Usage example:**

```java
@Autowired
private io.github.bsayli.openapi.client.generated.api.CustomerControllerApi customerApi;

public void createCustomer() {
  var req = new io.github.bsayli.openapi.client.generated.dto.CustomerCreateRequest()
      .name("Jane Doe")
      .email("jane@example.com");

  var resp = customerApi.createCustomer(req); // ApiResponseCustomerCreateResponse

  System.out.println(resp.getStatus());                      // 201
  System.out.println(resp.getData().getCustomer().getName()); // "Jane Doe"
}
```

### Option B — Manual Wiring (no Spring context)

```java
var rest = RestClient.builder().baseUrl("http://localhost:8084/customer-service").build();
var apiClient = new io.github.bsayli.openapi.client.generated.invoker.ApiClient(rest)
    .setBasePath("http://localhost:8084/customer-service");
var customerApi = new io.github.bsayli.openapi.client.generated.api.CustomerControllerApi(apiClient);
```

---

## 🧩 Adapter Pattern Example

For larger applications, encapsulate the generated API in an adapter:

```java
package io.github.bsayli.openapi.client.adapter.impl;

import io.github.bsayli.openapi.client.adapter.CustomerClientAdapter;
import io.github.bsayli.openapi.client.common.ApiClientResponse;
import io.github.bsayli.openapi.client.generated.api.CustomerControllerApi;
import io.github.bsayli.openapi.client.generated.dto.*;
import org.springframework.stereotype.Service;

@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

    private final CustomerControllerApi customerControllerApi;

    public CustomerClientAdapterImpl(CustomerControllerApi customerControllerApi) {
        this.customerControllerApi = customerControllerApi;
    }

    @Override
    public ApiClientResponse<CustomerCreateResponse> createCustomer(CustomerCreateRequest request) {
        return customerControllerApi.createCustomer(request);
    }

    @Override
    public ApiClientResponse<CustomerDto> getCustomer(Integer customerId) {
        return customerControllerApi.getCustomer(customerId);
    }

    @Override
    public ApiClientResponse<CustomerListResponse> getCustomers() {
        return customerControllerApi.getCustomers();
    }

    @Override
    public ApiClientResponse<CustomerUpdateResponse> updateCustomer(Integer customerId, CustomerUpdateRequest request) {
        return customerControllerApi.updateCustomer(customerId, request);
    }

    @Override
    public ApiClientResponse<CustomerDeleteResponse> deleteCustomer(Integer customerId) {
        return customerControllerApi.deleteCustomer(customerId);
    }
}
```

This ensures:

* Generated code stays isolated.
* Business code depends only on the adapter interface.
* Naming conventions are consistent with the service (createCustomer, getCustomer, getCustomers, updateCustomer, deleteCustomer).

---

## 🧩 How the Generics Work

The template at `src/main/resources/openapi-templates/api_wrapper.mustache` emits wrappers like:

```java
import io.github.bsayli.openapi.client.common.ApiClientResponse;

// e.g., ApiResponseCustomerCreateResponse
public class ApiResponseCustomerCreateResponse
        extends ApiClientResponse<CustomerCreateResponse> {
}
```

Only this Mustache partial is customized. All other models use stock templates.

---

## 🧪 Tests

Integration test with MockWebServer:

```bash
mvn -q -DskipITs=false test
```

It enqueues responses for **all CRUD operations** and asserts correct mapping into the respective wrappers (e.g. `ApiResponseCustomerCreateResponse`, `ApiResponseCustomerUpdateResponse`).

---

## 📚 Notes

* Dependencies like `spring-web`, `spring-context`, `jackson-*`, `jakarta.*` are marked **provided**; your host app supplies them.
* Generator options: Spring 6 `RestClient`, Jakarta EE, Jackson, Java 21.
* OpenAPI spec path:

```
src/main/resources/customer-api-docs.yaml
```

---

## 🛡 License

This repository is licensed under **MIT** (root `LICENSE`). Submodules inherit the license.
