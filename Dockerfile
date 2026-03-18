# Caravee Camel Runtime
#
# 100% Apache artifacts assembled from Maven Central.
# No Caravee business logic here — only Camel Quarkus extensions.
#
# Verify yourself:
#   docker build -t caravee-camel-runtime .
#   docker run --rm caravee-camel-runtime sha256sum /app/quarkus-run.jar

FROM maven:3.8.8-eclipse-temurin-21 AS build

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -q 2>/dev/null || true
COPY src ./src
RUN mvn package -DskipTests -q

# ── Runtime: official Eclipse Temurin JRE ─────────────────────────────────
FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /build/target/quarkus-app /app

# SBOM — full dependency list for auditing (CycloneDX, outputName=sbom)
COPY --from=build /build/target/sbom.json /app/sbom.json

RUN mkdir -p /data/routes

VOLUME /data

EXPOSE 8090

CMD ["java", "-jar", "/app/quarkus-run.jar"]
