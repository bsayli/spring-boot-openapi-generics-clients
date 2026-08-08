# customer-service-client

> Minimal reference consumer for **contract-aligned, generics-aware OpenAPI clients**

This sample shows only one thing:

> How to generate a Java client from the published OpenAPI and use it safely behind an adapter.

---

## 🚀 Quick Start

### 1. Inherit the parent

```xml
<parent>
  <groupId>io.github.blueprint-platform</groupId>
  <artifactId>openapi-generics-java-codegen-parent</artifactId>
  <version>1.2.1</version>
</parent>
```

### 2. Configure generator

```xml
<plugin>
  <groupId>org.openapitools</groupId>
  <artifactId>openapi-generator-maven-plugin</artifactId>

  <executions>
    <execution>
      <id>generate-client</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>generate</goal>
      </goals>

      <configuration>
        <generatorName>java-generics-contract</generatorName>
        <inputSpec>${project.basedir}/src/main/resources/customer-api-docs.yaml</inputSpec>

        <library>restclient</library>

        <apiPackage>com.example.generated.api</apiPackage>
        <modelPackage>com.example.generated.dto</modelPackage>
        <invokerPackage>com.example.generated.invoker</invokerPackage>
      </configuration>
    </execution>
  </executions>
</plugin>
```

### 3. Build

```bash
mvn clean install
```

---

## 📌 Rules (only these matter)

✔ Generate with:

```text
java-generics-contract
```

✔ Keep generated code behind an adapter

✔ Reuse external contract DTOs with BYOC when needed

❌ Do NOT:

* call generated APIs directly from application code
* duplicate envelope models
* let generated types leak into your domain boundary

---

## 🧩 Optional contract alignment

### BYOC — external DTO reuse

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.response-contract.CustomerDto=io.example.contract.CustomerDto
  </additionalProperty>
</additionalProperties>
```

### BYOE — custom envelope

```xml
<additionalProperties>
  <additionalProperty>
    openapi-generics.envelope=io.example.contract.ApiResponse
  </additionalProperty>
</additionalProperties>
```

Notes:

* BYOE is needed only if the service uses a custom envelope
* the custom envelope must already be available as a dependency
* if not configured, the default `ServiceResponse<T>` path is used

---

## 🔍 What to look at

### Generated client

```text
target/generated-sources/openapi/src/gen/java
```

### Adapter boundary

```java
public interface CustomerClientAdapter {
  ServiceResponse<CustomerDto> getCustomer(Integer customerId);
}
```

```java
@Service
public class CustomerClientAdapterImpl implements CustomerClientAdapter {

  private final CustomerControllerApi api;

  public CustomerClientAdapterImpl(CustomerControllerApi api) {
    this.api = api;
  }

  @Override
  public ServiceResponse<CustomerDto> getCustomer(Integer customerId) {
    return api.getCustomer(customerId);
  }
}
```

---

## ⚠️ Error handling

This sample handles upstream errors as:

```text
ProblemDetail → ApiProblemException
```

It also includes fallbacks for:

* empty error body
* non-JSON error response
* unparsable problem response

---

## 🧠 Mental Model

```text
OpenAPI → generated client → adapter → application
```

Generated code is replaceable.
Your adapter is the stable boundary.