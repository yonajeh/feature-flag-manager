# Feature Flag Manager

Standalone boolean feature-flag service with a Quarkus API, Angular admin UI, and machine-to-machine consumer API — all in **one container image**.

Use it to manage feature flags per application and read them from your services at runtime.

## Quick start

Replace `<username>` with the Docker Hub account that publishes this image (see your organization's registry URL).

```bash
docker pull <username>/feature-flag-manager:latest

docker run -d \
  --name feature-flag-manager \
  -p 8080:8080 \
  -v ffm_data:/data \
  -e FF_ADMIN_USERNAME=admin \
  -e FF_ADMIN_PASSWORD='change-me' \
  -e FF_TOKEN_PEPPER='change-me-to-a-long-random-secret' \
  <username>/feature-flag-manager:latest
```

Open **http://localhost:8080** and sign in with your admin credentials.

## What you get

| Endpoint | Purpose |
|----------|---------|
| `http://localhost:8080/` | Admin UI (applications, flags, API tokens) |
| `http://localhost:8080/q/health` | Health check (liveness/readiness) |
| `http://localhost:8080/q/openapi` | OpenAPI document |
| `http://localhost:8080/q/swagger-ui` | Interactive API explorer |

Data is stored in SQLite at `/data/ffm.db` inside the container. Mount a volume on `/data` to persist flags across restarts.

## Image tags

| Tag | When to use |
|-----|-------------|
| `latest` | Current release from the default branch |
| `v1.0.0` (example) | Pin to a specific release tag |
| `master` | Latest build from the `master` branch |
| `<git-sha>` | Exact commit build (for debugging) |

Prefer a version tag (`v*`) in production instead of `latest`.

## Docker Compose

Save as `docker-compose.yml` if you do not have the source repository:

```yaml
services:
  feature-flag-manager:
    image: <username>/feature-flag-manager:latest
    ports:
      - "8080:8080"
    volumes:
      - ffm_data:/data
    environment:
      FF_DB_PATH: /data/ffm.db
      FF_ADMIN_USERNAME: admin
      FF_ADMIN_PASSWORD: change-me
      FF_TOKEN_PEPPER: change-me-to-a-long-random-secret
      FF_JWT_ISSUER: feature-flag-manager
      FF_JWT_AUDIENCE: feature-flag-admin
    restart: unless-stopped

volumes:
  ffm_data:
```

```bash
docker compose up -d
```

## Environment variables

| Variable | Description | Default |
|----------|-------------|---------|
| `FF_DB_PATH` | SQLite database file path | `/data/ffm.db` |
| `FF_ADMIN_USERNAME` | Super-admin username | `admin` |
| `FF_ADMIN_PASSWORD` | Super-admin password | `admin` |
| `FF_TOKEN_PEPPER` | Server-side pepper for hashing API tokens | *(must set in production)* |
| `FF_JWT_ISSUER` | JWT issuer claim | `feature-flag-manager` |
| `FF_JWT_AUDIENCE` | JWT audience claim | `feature-flag-admin` |
| `FF_JWT_PUBLIC_KEY_PATH` | Classpath path to JWT public key | `META-INF/resources/publicKey.pem` |
| `FF_JWT_PRIVATE_KEY_PATH` | Classpath path to JWT private key | `META-INF/resources/privateKey.pem` |

### Production checklist

1. Set strong values for `FF_ADMIN_PASSWORD` and `FF_TOKEN_PEPPER`.
2. Mount a named volume (or bind mount) on `/data` so flags survive container recreation.
3. Pin an explicit image tag instead of `latest`.
4. Put the service behind HTTPS (reverse proxy or ingress); the container serves HTTP on port `8080`.
5. Restrict network access to the admin UI; only your apps need the consumer API.

## Admin workflow

1. Open the admin UI and log in.
2. Create an **application** (each consuming service gets its own app and flag namespace).
3. Create **feature flags** for that application.
4. Generate an **API token** — the plaintext token (`ff_live_...`) is shown **once**. Store it in your app's secrets manager.

## Consumer API (for application developers)

Your services read flags with machine-to-machine authentication. Send these headers on every request:

```http
X-App-Name: <application-slug>
X-App-Token: <ff_live_...>
```

### List all flags

```bash
curl -s \
  -H "X-App-Name: my-app" \
  -H "X-App-Token: ff_live_xxxxxxxx" \
  http://localhost:8080/api/v1/features
```

Example response:

```json
[
  { "key": "new-checkout", "enabled": true },
  { "key": "dark-mode", "enabled": false }
]
```

### Get one flag

```bash
curl -s \
  -H "X-App-Name: my-app" \
  -H "X-App-Token: ff_live_xxxxxxxx" \
  http://localhost:8080/api/v1/features/new-checkout
```

Example response:

```json
{ "key": "new-checkout", "enabled": true }
```

### Typical integration pattern

```text
App startup / cache refresh
        │
        ▼
GET /api/v1/features  (or single flag by key)
        │
        ▼
Cache in memory (TTL optional)
        │
        ▼
if (flags["new-checkout"].enabled) { ... }
```

Poll or refresh on an interval that fits your rollout needs. There is no push/webhook channel in v1.

## Admin REST API

Administrators use JWT bearer tokens from `POST /api/admin/auth/login`:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/admin/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"change-me"}' \
  | jq -r .token)

curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/admin/applications
```

Full contract: `http://localhost:8080/q/openapi` (or Swagger UI at `/q/swagger-ui`).

## Health checks

```bash
curl -s http://localhost:8080/q/health
```

Use this endpoint for Docker `HEALTHCHECK`, Kubernetes probes, or load balancer health tests.

Example `HEALTHCHECK` in a derived image or orchestrator config:

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/q/health || exit 1
```

## Volumes

| Path | Purpose |
|------|---------|
| `/data` | SQLite database (`ffm.db`) and all persisted state |

Back up `/data/ffm.db` regularly. To migrate, stop the container, copy the file, and start a new instance with the same volume or file.

## Ports

| Port | Protocol | Description |
|------|----------|-------------|
| `8080` | HTTP | Admin UI, consumer API, health, OpenAPI |

Map host port as needed, e.g. `-p 9080:8080`.

## Source and issues

Built from the [Feature Flag Manager](https://github.com) repository. For bugs, feature requests, or contribution guidelines, use the project's GitHub issue tracker.
