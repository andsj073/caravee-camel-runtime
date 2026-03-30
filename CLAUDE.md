# CLAUDE.md — caravee-camel-runtime

Apache Camel Quarkus runtime. **Pure Apache Foundation artifacts — no Caravee business logic.**

---

## Team

- **Andreas** — Product Owner
- **Mo** — Architect + Ops
- **Claude Code** — Dev Lead

**IMPORTANT:** Changes to this repo are rare and surgical. The whole point is that this is an auditable, dependency-clean runtime. Do not add Caravee-specific code here.

---

## What this is

A Quarkus app that assembles Camel extensions from Maven Central.
The Caravee agent ([caravee-camel-agent](../caravee-camel-agent)) runs as a sidecar alongside it.

```
caravee-camel-runtime (this)     ← Apache Foundation only
        ↓ file watch
/data/routes/*.yaml              ← agent writes resolved YAML here
        ↑
caravee-camel-agent (sidecar)    ← our code
```

---

## Key Configuration

`src/main/resources/application.properties`:
```properties
quarkus.http.port=8090
camel.main.routes-include-pattern=file:/data/routes/*.yaml
camel.main.routes-reload-enabled=true
camel.main.routes-reload-directory=/data/routes
```

Routes are hot-reloaded from `/data/routes/` — no restart needed for integration deploys.

---

## Camel Extensions

| Extension | Purpose |
|---|---|
| `camel-quarkus-yaml-dsl` | Hot-reload YAML routes |
| `camel-quarkus-kamelet` | Kamelet runtime |
| `camel-quarkus-http` / `platform-http` | HTTP client + server |
| `camel-quarkus-file` | File source/sink |
| `camel-quarkus-kafka` | Kafka source/sink |
| `camel-quarkus-timer` / `log` | Timer + log |
| `camel-quarkus-jackson` | JSON marshalling |
| `camel-quarkus-csv` | CSV parsing |
| `camel-quarkus-jdbc` + postgresql | JDBC sink |
| `quarkus-smallrye-health` | `/health/live` + `/health/ready` |
| `quarkus-micrometer-registry-prometheus` | `/metrics` (Prometheus) |

---

## Build

```bash
mvn package -DskipTests

# Docker
docker build -t caravee-camel-runtime .
```

Output: `target/quarkus-app/quarkus-run.jar`

---

## Verify integrity

```bash
# Build yourself and compare SHA
docker build -t caravee-camel-runtime .
docker run --rm caravee-camel-runtime sha256sum /app/quarkus-run.jar
```

CycloneDX SBOM generated on every CI build.

---

## What NOT to add here

- No Caravee-specific Java code
- No custom REST endpoints for cloud communication (that's the agent's job)
- No business logic, no data processing
- No dependencies outside Maven Central

If you think something belongs here, it almost certainly belongs in caravee-camel-agent instead.
