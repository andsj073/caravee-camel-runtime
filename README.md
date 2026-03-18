# caravee-camel-runtime

Apache Camel Quarkus runtime used by the Caravee engine stack.

## What this is

A Quarkus application that assembles Apache Camel extensions from Maven Central.  
**No Caravee business logic** — only Apache Foundation artifacts.

The Caravee agent ([caravee-camel-agent](https://github.com/andsj073/caravee-camel-agent)) runs as a sidecar alongside this runtime.

## Verify yourself

Every JAR comes from Maven Central, signed by the Apache Software Foundation.  
A [CycloneDX SBOM](https://cyclonedx.org/) is generated and attached to every CI build.

```bash
# Build it yourself and compare
docker build -t caravee-camel-runtime .
docker run --rm caravee-camel-runtime sha256sum /app/quarkus-run.jar
```

## Camel extensions included

| Extension | Purpose |
|-----------|---------|
| `camel-quarkus-yaml-dsl` | Hot-reload of route YAML files from `/data/routes/` |
| `camel-quarkus-kamelet` | Kamelet runtime |
| `quarkus-smallrye-health` | `/observe/health/live` + `/observe/health/ready` |
| `quarkus-micrometer-registry-prometheus` | `/observe/metrics` (Prometheus) |
| `camel-quarkus-micrometer` | Route-level metrics (exchanges, duration) |
| `camel-quarkus-platform-http` | Webhook / HTTP server source |
| `camel-quarkus-http` | HTTP client sink |
| `camel-quarkus-file` | File source (poll directory, move processed) |
| `camel-quarkus-csv` | CSV parsing |
| `camel-quarkus-jdbc` + `postgresql` | JDBC sink |
| `camel-quarkus-kafka` | Kafka source/sink |
| `camel-quarkus-timer` | Timer source |
| `camel-quarkus-log` | Log sink |
| `camel-quarkus-jackson` | JSON marshalling |
| `camel-quarkus-groovy` | Groovy scripting in transformers |

## Endpoints

| Path | Description |
|------|-------------|
| `GET /observe/health/live` | Liveness check |
| `GET /observe/health/ready` | Readiness check |
| `GET /observe/metrics` | Prometheus metrics |

## Related repos

| Repo | Description |
|------|-------------|
| [caravee-camel-agent](https://github.com/andsj073/caravee-camel-agent) | Go agent — pairs with cloud, deploys routes, monitors |
| [caravee](https://github.com/andsj073/caravee) | Cloud backend + frontend |
