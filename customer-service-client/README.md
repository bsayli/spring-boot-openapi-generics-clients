# customer-service-client

Generated Java client for the demo **customer-service**, showcasing **type‑safe generic responses** with OpenAPI + a tiny custom template (wrapping payloads in a reusable `ApiClientResponse<T>`).

---

## ✅ What you get

* Generated code using **OpenAPI Generator** (`restclient` with Spring Framework `RestClient`)
* A thin wrapper class per endpoint (e.g. `ApiResponseCustomerCreateResponse`) that **extends**:

    * `src/main/java/com/example/demo/client/common/ApiClientResponse.java`
* Minimal Spring wiring to expose the generated API as beans:

    * `com.example.demo.client.adapter.config.CustomerApiClientConfig`
* A focused integration test with **OkHttp MockWebServer**:

    * `com.example.demo.client.adapter.CustomerClientIT`

---

## 🧪 Quick pipeline (3 steps)

1. **Run the sample service**

```bash
cd customer-service
mvn spring-boot:run
# Service base URL: http://localhost:8084/customer
```

2. **Pull the OpenAPI spec into this module**

```bash
cd customer-service-client
curl -s http://localhost:8084/customer/v3/api-docs.yaml \
  -o src/main/resources/customer-api-docs.yaml
```

> You can also skip this if the spec is already checked in.

3. **Generate & build the client**

```bash
mvn clean install
```

Generated sources will land under:

```
target/generated-sources/openapi/src/gen/java/main
```

---

## 🚀 Using the client in your application

### Option A — Spring configuration (recommended)

Add this module as a dependency and set the base URL. The module contributes a small configuration:

```java
@Configuration
public class CustomerApiClientConfig {
  @Bean
  public RestClient customerRestClient(RestClient.Builder builder,
                                       @Value("${customer.api.base-url}") String baseUrl) {
    return builder.baseUrl(baseUrl).build();
  }

  @Bean
  public com.example.demo.client.generated.invoker.ApiClient customerApiClient(
      RestClient customerRestClient,
      @Value("${customer.api.base-url}") String baseUrl) {
    return new com.example.demo.client.generated.invoker.ApiClient(customerRestClient)
        .setBasePath(baseUrl);
  }

  @Bean
  public com.example.demo.client.generated.api.CustomerControllerApi customerControllerApi(
      com.example.demo.client.generated.invoker.ApiClient apiClient) {
    return new com.example.demo.client.generated.api.CustomerControllerApi(apiClient);
  }
}
```

**Configure the base URL** in your app:

```properties
customer.api.base-url=http://localhost:8084/customer
```

**Call the API**:

```java
@Autowired
private com.example.demo.client.generated.api.CustomerControllerApi customerApi;

public void createCustomer() {
  var req = new com.example.demo.client.generated.dto.CustomerCreateRequest()
      .name("Jane Doe")
      .email("jane@example.com");

  var resp = customerApi.create(req); // ApiResponseCustomerCreateResponse
  System.out.println(resp.getStatus());                 // 201
  System.out.println(resp.getData().getCustomer().getName()); // "Jane Doe"
}
```

### Option B — Manual wiring (no Spring context)

```java
var rest = RestClient.builder().baseUrl("http://localhost:8084/customer").build();
var apiClient = new com.example.demo.client.generated.invoker.ApiClient(rest)
    .setBasePath("http://localhost:8084/customer");
var customerApi = new com.example.demo.client.generated.api.CustomerControllerApi(apiClient);
```

---

## 🧩 How the generics work

The template at `src/main/resources/openapi-templates/api_wrapper.mustache` emits thin wrappers like:

```java
// e.g., ApiResponseCustomerCreateResponse
public class ApiResponseCustomerCreateResponse
    extends com.example.demo.client.common.ApiClientResponse<CustomerCreateResponse> { }
```

Only `api_wrapper.mustache` is customized for this demo; **all other models** use the stock templates/behavior.

---

## 🧪 Tests

Run the integration-style test with MockWebServer:

```bash
mvn -q -DskipITs=false test
```

It enqueues a `201` response and asserts mapping into `ApiResponseCustomerCreateResponse`.

---

## 📚 Notes

* Dependencies like `spring-web`, `spring-context`, `jackson-*`, `jakarta.*` are marked **provided**. Your host app supplies them.
* Generator options: Spring 6 `RestClient`, Jakarta EE, Jackson, Java 21.
* OpenAPI spec path used by the build:

```
src/main/resources/customer-api-docs.yaml
```

---

## 🛡 License

This repository is licensed under **MIT** (root `LICENSE`). Submodules don’t duplicate license files; the root license applies.
