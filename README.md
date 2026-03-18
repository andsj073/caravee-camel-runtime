# caravee-camel-runtime

Camel Quarkus runtime used by the Caravee engine.

## What this is

A Quarkus application that assembles Apache Camel extensions from Maven Central.  
**No Caravee business logic** — only Apache Foundation artifacts.

## Verify yourself

```bash
docker build -t caravee-camel-runtime .
# Every JAR comes from Maven Central, signed by Apache
```

Or inspect the SBOM (Software Bill of Materials) attached to each release.

## Extensions included

| Extension | Purpose |
|-----------|---------|
| camel-quarkus-yaml-dsl | Hot-reload of route YAML files |
| camel-quarkus-kamelet | Kamelet runtime |
| quarkus-smallrye-health | /observe/health/live + /ready |
| quarkus-micrometer-registry-prometheus | /observe/metrics |
| camel-quarkus-micrometer | Route-level metrics |
| camel-quarkus-platform-http | Webhook source |
| camel-quarkus-http | HTTP sink |
| camel-quarkus-file | File source |
| camel-quarkus-csv | CSV parsing |
| camel-quarkus-jdbc + postgresql | JDBC sink |
| camel-quarkus-kafka | Kafka source/sink |
| camel-quarkus-timer | Timer source |
| camel-quarkus-log | Log sink |
| camel-quarkus-jackson | JSON |
| camel-quarkus-groovy | Groovy scripting |

## Usage

See [caravee-engine](https://github.com/andsj073/caravee-camel-engine) for the full stack.
