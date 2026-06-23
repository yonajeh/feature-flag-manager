# Feature Flag Manager

Standalone boolean feature-flag service with a Quarkus backend, Angular admin UI, and machine-to-machine consumer API. Ships as a **single Docker image** (API + UI + migrations).

## Quick start (Docker Compose)

```bash
make compose-up
```

- Admin UI + APIs: http://localhost:8080
- Login: `admin` / `admin` (change via env vars)
- Health: http://localhost:8080/q/health
- OpenAPI: http://localhost:8080/q/openapi

Stop: `make compose-down`

## Repository layout

```
api/openapi.yaml          # API-first contract
apps/backend/             # Quarkus (Java 21)
apps/frontend/            # Angular + Tailwind admin UI
docker/                   # Dockerfile + docker-compose.yml
docs/                     # Developer guides
```

## Development

See [docs/README.md](docs/README.md) for local dev, testing, and build details.

**Requires Java 21** — set `JAVA_HOME` before running Maven (`make backend-test` does this on macOS).

```bash
make test           # backend + frontend unit/integration tests
make docker-build   # single-container image
```

## Environment variables

| Variable | Description | Default |
|----------|-------------|---------|
| `FF_DB_HOST` | PostgreSQL host | `localhost` (prod profile) |
| `FF_DB_PORT` | PostgreSQL port | `5432` |
| `FF_DB_NAME` | Database name | `feature_flags` |
| `FF_DB_USER` | Database user | `ffm` |
| `FF_DB_PASSWORD` | Database password | `ffm` |
| `FF_ADMIN_USERNAME` | Super-admin username | `admin` |
| `FF_ADMIN_PASSWORD` | Super-admin password | `admin` |
| `FF_TOKEN_PEPPER` | Server-side pepper for API token hashing | (required in prod) |
| `FF_JWT_ISSUER` | JWT issuer | `feature-flag-manager` |
| `FF_JWT_AUDIENCE` | JWT audience | `feature-flag-admin` |
| `FF_JWT_PUBLIC_KEY_PATH` | JWT public key resource path | `META-INF/resources/publicKey.pem` |
| `FF_JWT_PRIVATE_KEY_PATH` | JWT private key resource path | `META-INF/resources/privateKey.pem` |
| `FF_APP_PORT` | Host port for compose | `8080` |

## APIs

### Consumer (M2M)

```
X-App-Name: <application.slug>
X-App-Token: <ff_live_...>
```

- `GET /api/v1/features` — list flags for the authenticated app
- `GET /api/v1/features/{key}` — get one flag

### Admin (JWT)

Login via `POST /api/admin/auth/login`, then use `Authorization: Bearer <jwt>` on `/api/admin/**`.

Manage applications, feature flags, and API tokens (generate / rotate / revoke). Plaintext tokens are returned **once** on create/rotate.

## License

Internal / project use.
