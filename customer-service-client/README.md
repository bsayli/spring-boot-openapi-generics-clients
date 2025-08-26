# customer-service-client

A minimal Java client for the demo **customer-service**, showing how to use **type-safe generic responses** with OpenAPI
and a custom template (`ApiClientResponse<T>`).

---

## 📦 Contents

* Generated sources via **OpenAPI Generator** (`restclient` using Spring `RestClient`)
* Custom wrapper generation using `ApiClientResponse<T>`
* Spring config for wiring (`CustomerApiClientConfig`)
* Integration test with **MockWebServer** (`CustomerClientIT`)

---

## 🔧 Requirements

* JDK 21
* Maven 3.9+
* OpenAPI spec at:

```
src/main/resources/customer-api-docs.yaml
```

To refresh from a running service:

```bash
curl -s http://localhost:8084/customer/v3/api-docs.yaml \
  -o src/main/resources/customer-api-docs.yaml
```

---

## 🚀 Build & Generate

Run:

```bash
mvn clean install
```

Generated sources land in:

```
target/generated-sources/openapi/src/gen/java/main
```

---

## 🧩 Generics support

Custom templates ensure wrappers extend one reusable class:

```java
public class ApiResponseCustomerCreateResponse
        extends com.example.demo.client.common.ApiClientResponse<CustomerCreateResponse> {
}
```

This keeps response envelopes consistent and type-safe.

---

## 🧪 Tests

Integration test `CustomerClientIT` runs against a **MockWebServer**, verifying that a `201 CREATED` response maps
correctly into `ApiResponseCustomerCreateResponse`.

Run tests with:

```bash
mvn test
```

---

## 🧰 Usage

Add config to your application:

```java
@Configuration
public class CustomerApiClientConfig {
  @Bean
  RestClient customerRestClient(RestClient.Builder builder,
                                @Value("${customer.api.base-url}") String baseUrl) {
    return builder.baseUrl(baseUrl).build();
  }

  @Bean
  CustomerControllerApi customerControllerApi(ApiClient apiClient) {
    return new CustomerControllerApi(apiClient);
  }
}
```

Use in code:

```java
@Autowired
private CustomerControllerApi customerApi;

public void createCustomer() {
  var req = new CustomerCreateRequest().name("Jane Doe").email("jane@example.com");
  var resp = customerApi.create(req);
  System.out.println(resp.getStatus()); // 201
}
```

Property required:

```properties
customer.api.base-url=http://localhost:8084/customer
```

---

## 📚 Notes

* Core deps (`spring-web`, `spring-context`, `jackson-*`, `jakarta.*`) are **provided**.
* Custom templates:

    * `api_wrapper.mustache` (main customization)
    * `model.mustache` (delegates to wrapper conditionally)
* Others use stock OpenAPI Generator templates.

---

## 🛡 License

MIT
