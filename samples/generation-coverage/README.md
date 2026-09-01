# generation-coverage

> Build-time verification samples for OpenAPI Generics generation topologies.

The runtime-oriented samples validate what happens across producer, OpenAPI, generated client, and
consumer boundaries. `generation-coverage` focuses on a different question:

> **Can OpenAPI Generics be used safely in non-trivial Maven generation layouts?**

The initial sample verifies a common real-world setup in which one Maven client module generates
code from more than one OpenAPI specification.

## Table of Contents

- [Purpose](#purpose)
- [Available Samples](#available-samples)
- [Validation Scope](#validation-scope)
- [Running the Samples](#running-the-samples)
- [Design Boundary](#design-boundary)

## Purpose

The sample families have distinct responsibilities:

```text
type-coverage
    → generic contract shape fidelity

transport-coverage
    → standard HTTP transport compatibility

generation-coverage
    → build-time generator topology and isolation
```

`generation-coverage` exists to verify Maven and OpenAPI Generator integration patterns that are
not naturally exercised by a single producer-to-consumer runtime flow.

## Available Samples

| Sample | Purpose |
| --- | --- |
| [`multi-spec-maven-client`](multi-spec-maven-client/README.md) | Runs two independent OpenAPI Generics Maven executions in one client module and compiles both generated source trees into one artifact. |

## Validation Scope

The suite is intended to cover build-time concerns such as:

- multiple generator executions in one Maven module;
- isolated output directories;
- isolated API, model, and invoker packages;
- safe use of `cleanupOutput`;
- execution-specific BYOC configuration;
- compilation of all generated source trees in one artifact.

## Running the Samples

From the `samples` directory:

```bash
mvn -pl generation-coverage -am clean verify
```

## Design Boundary

These samples validate build topology.

They intentionally do not introduce:

- additional producer applications;
- consumer applications;
- runtime HTTP communication;
- transport-specific behavior;
- duplicate OpenAPI specifications.

Existing committed specifications are reused as authoritative inputs wherever possible.
