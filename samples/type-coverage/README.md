# type-coverage

Focused end-to-end validation samples for **OpenAPI Generics**.

Unlike the Spring Boot integration samples, the projects in this
directory are not intended to demonstrate application architecture,
business workflows, or framework usage patterns.

Their purpose is to provide deterministic validation environments for
verifying the core OpenAPI Generics pipeline and preventing regressions
across future releases.

The samples validate that generic response contracts move through the
complete lifecycle while preserving both **contract ownership** and
**generic type identity**.

``` text
Producer
    ↓
OpenAPI Projection
    ↓
Generated Client
    ↓
Consumer Runtime
```

------------------------------------------------------------------------

# Table of Contents

-   Purpose
-   Available Samples
-   Sample Boundaries
-   Validation Scope
-   Regression Role
-   Mental Model

------------------------------------------------------------------------

# Purpose

The `type-coverage` suite exists to verify that OpenAPI Generics
preserves Java contract semantics throughout the entire pipeline.

It focuses on:

-   OpenAPI projection correctness
-   vendor extension generation
-   generated client reconstruction
-   runtime deserialization
-   consumer-side type compatibility
-   contract ownership preservation
-   regression protection across future releases

The objective is not endpoint functionality.

The objective is deterministic verification of generic contract
fidelity.

------------------------------------------------------------------------

# Available Samples

  ----------------------------------------------------------------------------------------
Sample                                             Purpose
  -------------------------------------------------- -------------------------------------
[`service-response`](service-response/README.md)   Validates the built-in
`ServiceResponse<T>` envelope with
both platform-provided and
application-owned generic containers.

[`byoe-response`](byoe-response/README.md)         Validates Bring Your Own Envelope
(BYOE) using a user-owned
`ApiResponse<T>` contract together
with application-owned generic
containers.
  ----------------------------------------------------------------------------------------

Each sample contains its own producer, generated client and consumer.

------------------------------------------------------------------------

# Sample Boundaries

## service-response

Validates the platform envelope:

``` java
ServiceResponse<T>
```

Coverage includes:

``` java
ServiceResponse<T>
ServiceResponse<List<T>>
ServiceResponse<Set<T>>
ServiceResponse<Page<T>>
ServiceResponse<Window<T>>
ServiceResponse<Batch<T>>
```

This sample demonstrates that application-owned generic containers can
be reconstructed while continuing to use the built-in platform envelope.

------------------------------------------------------------------------

## byoe-response

Validates a completely application-owned envelope:

``` java
ApiResponse<T>
```

Coverage includes:

``` java
ApiResponse<T>
ApiResponse<List<T>>
ApiResponse<Set<T>>
ApiResponse<Page<T>>
ApiResponse<Paging<T>>
ApiResponse<Window<T>>
```

This sample proves that both the envelope and nested generic containers
may be owned by the application while preserving generic semantics
end-to-end.

------------------------------------------------------------------------

# Validation Scope

Across the suite, the samples verify:

-   generic envelope reconstruction
-   scalar payload handling
-   value payload handling
-   enum payload handling
-   DTO payload handling
-   collection reconstruction
-   platform container reconstruction
-   application-owned generic container reconstruction
-   BYOE reconstruction
-   ignored model handling
-   external model handling
-   generated client type safety
-   runtime deserialization
-   producer → client → consumer compatibility

Each sample intentionally validates a different contract ownership
model.

------------------------------------------------------------------------

# Regression Role

These projects act as executable regression suites.

They are intended to detect regressions in:

-   response introspection
-   OpenAPI metadata generation
-   vendor extension consistency
-   generic wrapper reconstruction
-   generated Java typing
-   generated-source hygiene
-   runtime deserialization
-   end-to-end producer → client → consumer compatibility

before such regressions reach production integrations.

------------------------------------------------------------------------

# Mental Model

``` text
Original Java Contract
          ↓
OpenAPI Projection
          ↓
Generated Client
          ↓
Consumer Runtime
          ↓
Same Contract Shape
```

Platform envelope example:

``` text
ServiceResponse<Window<TypeSummaryDto>>
          ↓
OpenAPI Projection
          ↓
Generated Client
          ↓
ServiceResponse<Window<TypeSummaryDto>>
```

BYOE example:

``` text
ApiResponse<Paging<TypeSummaryDto>>
          ↓
OpenAPI Projection
          ↓
Generated Client
          ↓
ApiResponse<Paging<TypeSummaryDto>>
```

The purpose of the type-coverage suite is to continuously verify that
**generic contract shape is preserved**, regardless of whether the
envelope or nested generic containers are platform-provided or
application-owned.
