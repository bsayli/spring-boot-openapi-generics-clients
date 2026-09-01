# multi-spec-maven-client

> Build-time verification for generating two independent OpenAPI Generics clients from one Maven module.

Real client artifacts often consume more than one OpenAPI specification. This sample verifies that
multiple `openapi-generator-maven-plugin` executions remain isolated while contributing generated
sources to the same Maven artifact.

## Table of Contents

- [What This Sample Validates](#what-this-sample-validates)
- [Execution Topology](#execution-topology)
- [Input Specifications](#input-specifications)
- [Isolation Rules](#isolation-rules)
- [Compile-Time Verification](#compile-time-verification)
- [Running the Sample](#running-the-sample)
- [Expected Output](#expected-output)
- [Design Boundary](#design-boundary)
- [Mental Model](#mental-model)

## What This Sample Validates

The module verifies that:

- both executions run during `generate-sources`;
- each execution reads a different OpenAPI document;
- each execution has a unique Maven execution ID;
- generated output directories are independent;
- API, model, and invoker packages are independent;
- `cleanupOutput=true` does not remove the other execution's output;
- BYOC configuration remains scoped to its own execution;
- generated sources from both specifications compile into one artifact.

## Execution Topology

```text
customer-api-docs.yaml
        │
        ▼
generate-customer-client
        │
        ▼
target/generated-sources/customer
        │
        ▼
...generationcoverage.customer.*

────────────────────────────────────────

service-response-coverage.yaml
        │
        ▼
generate-service-response-client
        │
        ▼
target/generated-sources/service-response
        │
        ▼
...generationcoverage.serviceresponse.*
```

Both generated source trees participate in the same Maven compilation.

## Input Specifications

The sample reuses existing committed specifications:

```text
../../spring-boot-3/spec/customer-api-docs.yaml

../../type-coverage/service-response/spec/service-response-coverage.yaml
```

The specifications are not copied into this module. Their original producer samples remain the
authoritative owners.

## Isolation Rules

Each execution owns a separate:

- `output` directory;
- `apiPackage`;
- `modelPackage`;
- `invokerPackage`.

This prevents:

- generated source overwrites;
- package-name collisions;
- one execution's cleanup from deleting another execution's files;
- execution-specific configuration from leaking across specifications.

The customer execution also contains its own BYOC mapping for `CustomerDto`. The
service-response execution does not inherit or depend on that mapping.

## Compile-Time Verification

`MultiSpecGenerationProbe` imports representative generated types from both source trees.

The selected types cover:

- generated API classes from both executions;
- a BYOC-backed customer wrapper;
- a paged customer wrapper;
- direct and collection-based `ServiceResponse<T>` wrappers;
- built-in and application-owned generic container reconstruction.

The probe is intentionally representative rather than an inventory of every generated class.

If either execution does not run, writes to an unexpected package, or loses its output during
cleanup, normal Maven compilation fails.

## Running the Sample

From the `samples` directory:

```bash
mvn -pl generation-coverage/multi-spec-maven-client -am clean verify
```

## Expected Output

The build creates two generated roots:

```text
generation-coverage/multi-spec-maven-client/
└── target/
    └── generated-sources/
        ├── customer/
        │   └── src/main/java/...
        └── service-response/
            └── src/main/java/...
```

Representative reconstructed contracts include:

```java
ServiceResponse<CustomerDto>

ServiceResponse<Page<CustomerDto>>

ServiceResponse<TypeSummaryDto>

ServiceResponse<List<TypeSummaryDto>>
```

## Design Boundary

This sample validates Maven generation topology, not runtime communication.

It intentionally has no:

- producer application;
- consumer application;
- Spring Boot runtime;
- HTTP adapter;
- transport verification.

Runtime contract fidelity and transport compatibility remain the responsibility of the
`type-coverage` and `transport-coverage` samples.

## Mental Model

```text
Spec A
  ↓
Execution A
  ↓
Generated Tree A
  ┐
  ├──→ Maven Compile → Single Client Artifact
  ┘
Generated Tree B
  ↑
Execution B
  ↑
Spec B
```

The important guarantee is not merely that both executions start. It is that both generated outputs
remain independent, usable, and compilable inside the same Maven module.
