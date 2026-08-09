---
layout: default
title: Client-Side Adoption
parent: Adoption Guides
nav_order: 2
has_toc: false
---

# Client-Side Adoption

> Generate Java clients that reconstruct your published contract instead of redefining it.

This guide explains how to generate contract-aligned Java clients from an OpenAPI Generics document.

For server-side projection, see [Server-Side Adoption](./server-side-adoption.md).  
For architecture details, see [Architecture](../architecture/architecture.md).

---

## Contents

- [Quick Start](#quick-start)
- [Contract-Driven Envelope Reconstruction](#contract-driven-envelope-reconstruction)
- [BYOE — Bring Your Own Envelope](#byoe)
- [BYOC — Bring Your Own Contract](#byoc)
- [Application-Defined Generic Containers](#application-defined-generic-containers)
- [Supported Contracts](#supported-contracts)
- [Fallback Mode](#fallback-mode)
- [Verification](#verification)
- [Usage Boundary](#usage-boundary)
- [Further Reading](#further-reading)

---

## Quick Start

### 1. Inherit the codegen parent

```xml
<parent>
    <groupId>io.github.blueprint-platform</groupId>
    <artifactId>openapi-generics-java-codegen-parent</artifactId>
    <version>1.2.1</version>
</parent>
```

The parent provides the OpenAPI Generics-specific template preparation, generator integration, tested OpenAPI Generator alignment, source registration, and generated-source hygiene required for reconstruction.

---

### 2. Configure OpenAPI Generator

Use the OpenAPI Generator Maven plugin as usual, but select the OpenAPI Generics Java generator:

```xml
<generatorName>java-generics-contract</generatorName>
```

That is the OpenAPI Generics integration point.

Standard OpenAPI Generator options such as `library`, `apiPackage`, `modelPackage`, `invokerPackage`, and Spring/Jackson configuration remain normal generator choices.

Example:

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

                <inputSpec>
                    ${project.basedir}/src/main/resources/api-docs.yaml
                </inputSpec>

                <!-- Standard OpenAPI Generator choice. -->
                <library>restclient</library>

                <!-- Standard generated package layout. -->
                <apiPackage>com.example.client.api</apiPackage>
                <modelPackage>com.example.client.dto</modelPackage>
                <invokerPackage>com.example.client.invoker</invokerPackage>

                <configOptions>
                    <useSpringBoot3>true</useSpringBoot3>
                    <serializationLibrary>jackson</serializationLibrary>
                    <openApiNullable>false</openApiNullable>
                </configOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Expected generated wrapper shape:

```java
public class ServiceResponseCustomerDto
    extends ServiceResponse<CustomerDto> {
}
```

Container example:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {
}
```

`Page<T>` is imported from `openapi-generics-contract`.

The generated wrapper itself is emitted under the configured `modelPackage`.

---

### 3. Generate

```bash
mvn clean install
```

Generated sources are added automatically under:

```text
target/generated-sources/openapi/src/gen/java
```

The generator reconstructs supported wrapper and container contracts from OpenAPI Generics metadata in the input document.

---

## Contract-Driven Envelope Reconstruction

OpenAPI Generics reconstructs envelope identity from contract metadata carried by the OpenAPI document when producer and codegen components are aligned.

The producer publishes the envelope type through:

```yaml
x-api-wrapper-type: io.example.contract.ApiResponse
```

The Java generator consumes that metadata during wrapper reconstruction.

```text
Producer contract
      ↓
x-api-wrapper-type
      ↓
OpenAPI document
      ↓
java-generics-contract
      ↓
Generated wrapper
```

The generated OpenAPI document is therefore the source of reconstruction metadata for the client generator.

The same envelope does not need to be declared again on the client through `openapi-generics.envelope`.

This removes duplicated envelope configuration across producer and client generation.

The envelope type itself must still be available on the client classpath because generated wrappers extend the original contract type.

---

## BYOE — Bring Your Own Envelope

BYOE allows the generated client to reuse an application-owned response envelope instead of `ServiceResponse<T>`.

The envelope is configured on the producer side:

```yaml
openapi-generics:
  envelope:
    type: com.example.contract.ApiResponse
```

The generated OpenAPI document preserves that identity through:

```yaml
x-api-wrapper-type: com.example.contract.ApiResponse
```

The client generator then reconstructs wrappers such as:

```java
public class ApiResponseCustomerDto
    extends ApiResponse<CustomerDto> {
}
```

and:

```java
public class ApiResponsePageCustomerDto
    extends ApiResponse<Page<CustomerDto>> {
}
```

No duplicate `openapi-generics.envelope` declaration is required on the client when producer and codegen components are aligned.

The application-owned envelope must be available as a dependency of the generated client module.

---

## BYOC — Bring Your Own Contract

BYOC allows generated clients to reuse DTOs already owned by your application or shared contract modules instead of generating duplicate models.

Configure mappings through additional properties:

```xml
<additionalProperties>
    <additionalProperty>
        openapi-generics.response-contract.CustomerDto=com.example.contract.CustomerDto
    </additionalProperty>

    <additionalProperty>
        openapi-generics.response-contract.AddressDto=com.example.contract.AddressDto
    </additionalProperty>
</additionalProperties>
```

Each mapping follows:

```text
openapi-generics.response-contract.<OpenAPI model name>=<fully-qualified Java type>
```

Mapped models are imported directly from the shared contract module.

Example:

```java
public class ServiceResponseCustomerDto
    extends ServiceResponse<CustomerDto> {
}
```

where `CustomerDto` resolves to:

```java
com.example.contract.CustomerDto
```

instead of a generated duplicate.

BYOC remains a client-side mapping because it expresses which existing Java type should satisfy a particular OpenAPI model name in the generated source graph.

---

## Application-Defined Generic Containers

Application-defined generic containers are reconstructed from the metadata published in the OpenAPI document.

Producer configuration may define:

```yaml
openapi-generics:
  containers:
    - type: com.example.contract.Paging
      item-property: content

    - type: com.example.contract.Window
      item-property: items
```

The projected document preserves the container identity through metadata such as:

```yaml
x-data-container: Window
x-data-container-type: com.example.contract.Window
x-data-item: CustomerDto
```

The client generator uses `x-data-container-type` to reconstruct the Java container type without requiring container-specific client configuration.

Example generated wrapper:

```java
public class ServiceResponseWindowCustomerDto
    extends ServiceResponse<Window<CustomerDto>> {
}
```

With BYOE:

```java
public class ApiResponseWindowCustomerDto
    extends ApiResponse<Window<CustomerDto>> {
}
```

Application-defined container classes must be available on the generated client classpath.

Built-in and configured containers participate in the same reconstruction pipeline.

---

## Supported Contracts

Built-in response shapes include:

```java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
```

BYOE envelopes participate in the same supported response-shape model.

Application-defined generic containers published through OpenAPI Generics metadata also participate in the same reconstruction pipeline when their Java types are available on the client classpath.

Representative combinations include:

```java
ServiceResponse<Window<T>>
ApiResponse<Paging<T>>
```

The effective Java type is reconstructed from the contract metadata carried by the input OpenAPI document.

---

## Fallback Mode

OpenAPI Generics provides two levels of opt-out.

### Skip the generics-aware template lifecycle

Set:

```xml
<openapi.generics.skip>true</openapi.generics.skip>
```

This skips the template extraction, patching, and overlay lifecycle provided by `openapi-generics-java-codegen-parent`.

### Return fully to standard OpenAPI Generator

Use the upstream Java generator:

```xml
<generatorName>java</generatorName>
```

This removes OpenAPI Generics reconstruction behavior entirely and returns generation ownership to standard OpenAPI Generator.

Use fallback mode for:

- troubleshooting
- output comparison
- isolating generator behavior
- temporary opt-out scenarios

---

## Verification

After generation, verify the contract behavior that matters to the consuming module:

- generated wrappers extend the intended shared envelope instead of redefining it
- built-in or configured generic containers resolve to the intended Java contract types
- BYOC mappings reuse the configured external Java models instead of generating duplicates
- contract-owned infrastructure models are not regenerated
- generated sources compile against the required shared contract dependencies

Representative built-in wrapper:

```java
public class ServiceResponsePageCustomerDto
    extends ServiceResponse<Page<CustomerDto>> {
}
```

Representative BYOE wrapper:

```java
public class ApiResponseWindowCustomerDto
    extends ApiResponse<Window<CustomerDto>> {
}
```

Repository verification additionally covers metadata consumption, generated-source hygiene, standard Java RestClient transport compatibility, and the complete producer → OpenAPI → generated client → consumer lifecycle.

---

## Usage Boundary

Generated wrappers are transport bindings, not application contracts.

Application code should depend on shared contract types such as:

```java
ServiceResponse<CustomerDto>
```

or:

```java
ApiResponse<CustomerDto>
```

rather than generated wrapper classes.

Generated classes such as:

```java
ServiceResponseCustomerDto
```

exist to bind concrete OpenAPI response schemas to the generic Java contract during transport and deserialization.

Keep generated clients behind an adapter boundary when possible so application code remains independent of generated artifacts.

The contract owner should remain the shared Java contract module, not generated source.

---

## Further Reading

- [Server-Side Adoption](./server-side-adoption.md)
- [Architecture](../architecture/architecture.md)
- [Compatibility & Support Policy](../compatibility.md)
- [GitHub Discussions](https://github.com/blueprint-platform/openapi-generics/discussions)
