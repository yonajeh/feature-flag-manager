# Developer guide

## Prerequisites

- Java 21 (`JAVA_HOME` must point to JDK 21)
- Maven 3.9+
- Node.js 20+ and npm
- Docker (for integration tests, image build, and compose)

## Local backend (Dev Services PostgreSQL)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
cd apps/backend
mvn quarkus:dev
```

API: http://localhost:8080 — Swagger UI: http://localhost:8080/q/swagger-ui

Default super-admin credentials: `admin` / `admin` (override with `FF_ADMIN_*`).

## Local frontend (proxied to Quarkus)

Run the backend first, then:

```bash
cd apps/frontend
npm start
```

For production-like testing, build the UI into the backend static folder:

```bash
cd apps/frontend && npm run build
cp -R dist/frontend/* ../backend/src/main/resources/META-INF/resources/
```

## Tests

```bash
make backend-test    # unit + integration (Testcontainers)
make frontend-test   # Karma component tests
make test            # both
```

Integration tests launch the packaged JAR against a Testcontainers PostgreSQL instance.

## Docker

```bash
make docker-build
make compose-up      # app + postgres (+ adminer with --profile tools)
make compose-down
```

## API contract

Source of truth: `api/openapi.yaml`. The served document is at `/q/openapi`.
