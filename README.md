# DeptFlow WebAPI

Backend services for the **DeptFlow** department-workflow application: a multi-tenant REST API that manages institutions, departments, the people working in them, and the (always-versioned) documents those people use.

## What it does

- **Organizational model** — institutions (tenant boundary, optional umbrella hierarchy), nested departments with a head person, and people optionally linked to a login account.
- **Multi-tenancy** — many institutions share one schema; every tenant-owned record carries an `institution_id` and all queries are institution-scoped.
- **People & roles** — configurable per-institution roles and person→department memberships with a role, primary flag, and active date range (membership history is retained).
- **Versioned documents** — every document has immutable file versions. A version-level approval workflow (`Draft → PendingApproval → Approved|Rejected`) drives the document summary status; approved versions stay live while a new draft is edited beside them.
- **Department-scoped access** — a person can use a document when they hold an active membership in the document's owning department (no per-document ACL tables).
- **Backend-agnostic storage** — document files are referenced by an opaque `storage_key` behind a `StorageService` (filesystem by default).
- **JWT security** — `/register` and `/login` issue signed JWTs; protected endpoints require a Bearer token.

## Tech stack

- Java 25 (LTS), Spring Boot 4.1.x (Spring Framework 7)
- Spring MVC, Spring Security (JWT resource server)
- Spring Data JPA / Hibernate 7, Flyway
- Databases: SQLite (dev), PostgreSQL, SQL Server
- OpenAPI via springdoc, Testcontainers for multi-DB tests

## Architecture

A layered, persistence-ignorant design mirrored as Maven modules:

```
api  →  application  →  domain  ←  infrastructure
```

| Module | Responsibility |
| --- | --- |
| `domain` | Business models and invariants as pure POJOs; references nothing (no framework imports) |
| `application` | Use cases, ports (repositories/storage/tenant), DTOs, policies; references `domain` |
| `infrastructure` | JPA `orm.xml` mappings, repositories, storage, Flyway scripts; references `domain` + `application` |
| `api` | REST controllers, Spring Security, DI wiring; references `application` + `infrastructure` |

The domain stays free of JPA/Hibernate: Hibernate maps the POJOs entirely from `META-INF/orm.xml` in `infrastructure`.

## Building

Prerequisites: **JDK 25**. Maven is bootstrapped by the wrapper (`./mvnw`), so no local Maven install is needed.

```bash
./mvnw clean install              # build all modules and run the full test suite
./mvnw test                       # run tests only
./mvnw -pl api spring-boot:run    # run the API locally (SQLite, port 8080)
```

Run a single test class:

```bash
./mvnw -pl api -am test -Dtest=PostgresIntegrationTest
```

The suite is 25 tests: domain unit tests, application policy tests, H2 persistence/workflow integration tests, an API smoke test, and Testcontainers tests against real PostgreSQL and SQL Server.

**Regenerate the Flyway baseline migrations** (from `META-INF/orm.xml`; only while the schema is greenfield):

```bash
./mvnw -pl infrastructure -am -Pgenerate-schema process-test-classes
```

## Running

**Local (SQLite):**

```bash
./mvnw -pl api spring-boot:run
```

- App: `http://localhost:8080`
- OpenAPI: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

**Docker Compose (real database):**

```bash
docker-compose up --build                                  # app + PostgreSQL
DEPTFLOW_JWT_SECRET=... docker-compose up --build          # override the JWT secret
docker-compose -f docker-compose.sqlserver.yml up --build  # app + SQL Server
```

- App: `http://localhost:8080` · PostgreSQL: `localhost:5432` (db/user/password `deptflow`) · SQL Server: `localhost:1433` (user `sa`, set `MSSQL_SA_PASSWORD`)

## API & auth

Register and log in to obtain a token; pass it as `Authorization: Bearer <token>` on protected endpoints:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","email":"alice@example.com","password":"secret"}'

curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"secret"}'
```

Endpoints are grouped under `/api`: `institutions`, `departments`, `roles`, `people`, `memberships`, and `documents` (create with file upload, add versions, submit/approve/reject/archive). Full details in the Swagger UI.

## Configuration

- `spring.datasource.*` — database connection (SQLite by default)
- `spring.flyway.*` — Flyway migrations (enabled for PostgreSQL/SQL Server profiles)
- `deptflow.jwt.secret` — JWT signing key (override in production)
- `deptflow.storage.root-path` — filesystem storage root
- Spring profiles: `sqlite` (default), `postgres`, `sqlserver`

## Continuous integration (GitHub Actions)

`.github/workflows/ci.yml` runs on every push to `main`, every pull request, and manual runs:

1. **`test` job** — checks out the code, sets up Temurin JDK 25 (with Maven dependency caching), and runs `./mvnw verify`. This executes the whole suite: unit tests, H2 integration tests, and the Testcontainers PostgreSQL + SQL Server tests (Docker is preinstalled on the runner).
2. **`docker` job** — runs after `test` succeeds and executes `docker build -t deptflow-api .` to verify the deployment image still builds.
