# AGENTS.md - DeptFlow WebAPI

This file provides guidance to AI agents when working with the **deptflow-spring-api** repository. (It mirrors `CLAUDE.md`.)

## Repository Overview

**Purpose**: Backend services for the **DeptFlow** department workflow application. It manages institutions, departments, the people working in them, and the (versioned) documents those people use, exposed as a multi-tenant REST API.

**Status**: Migrating from .NET 10 to **Java 25 + Spring Boot**. The `migrate-to-spring-boot` OpenSpec change is the active change; the domain layer (Phase 1) is complete.

**Technology Stack**:
- Java 25 (LTS)
- Spring Boot 4.1.x (Spring Framework 7)
- Spring MVC (REST controllers)
- Spring Security (JWT resource server; replaces ASP.NET Core Identity)
- Spring Data JPA / Hibernate 7 (SQL Server / PostgreSQL / SQLite; SQLite by default in dev)
- Flyway (schema migrations)

## Architecture

Layered, persistence-ignorant design, mirrored as Maven modules:

```
api  →  application  →  domain  ←  infrastructure
```

- **domain**: business models as pure POJOs (no framework imports); references nothing.
- **application**: use cases and repository/storage ports (interfaces); references domain.
- **infrastructure**: JPA `orm.xml` mappings, repository implementations, and the storage provider; references domain + application.
- **api**: REST controllers and DI wiring; references application + infrastructure.

### Key design decisions

- **Persistence-ignorant domain**: one set of POJOs; Hibernate maps them via `META-INF/orm.xml` (field access) from Infrastructure — no `jakarta.persistence` imports in `domain`.
- **Person ≠ login**: `Person` is linked to `UserAccount` only through a nullable `userId`; the domain stays free of security concerns.
- **Multi-tenancy**: shared schema; tenant-owned entities carry `institution_id`, enforced with Hibernate discriminator multi-tenancy (`@TenantId`) which filters reads **and** stamps tenant ids on writes.
- **Department-scoped document access**: derived from active membership in the document's owning department; no per-document ACL tables.
- **Always-versioned documents**: `Document` (metadata/summary status) + `DocumentVersion` (immutable file facts).
- **Backend-agnostic storage**: files referenced by an opaque `storage_key` behind a `StorageService`.
- **Provider-agnostic persistence**: `UUID` PKs client-side, `@Version` optimistic locking, normalized lowercase unique codes, no provider-specific SQL.
- **Error handling**: domain exceptions + a `@RestControllerAdvice` returning RFC 7807 `ProblemDetail`.
- **Unit of work**: Spring `@Transactional` (no custom UoW class).

## Domain Model

| Entity | Purpose |
| --- | --- |
| Institution | Top-level organization (multi-tenant boundary; optional parent hierarchy) |
| Department | Owned by an institution; optional nesting; optional head person |
| Role | Configurable per-institution organizational role (distinct from Spring Security authorities) |
| Person | Organizational record, optionally linked to a login account (`userId`) |
| PersonDepartment | Membership: person in a department with role, primary flag, and date range (`startDate`/`endDate`) |
| Document | Department-owned document metadata and summary status (aggregate root) |
| DocumentVersion | Always-versioned file with version-level approval status and audit fields |
| DocumentType | Per-institution document categorization |
| UserAccount | Global login account (replaces ASP.NET Core Identity's `IdentityUser`) |

## Project Structure

```
deptflow-spring-api/
├── pom.xml                # parent Maven POM (modules + Spring Boot dependency management)
├── mvnw / mvnw.cmd        # Maven wrapper (self-bootstrapping)
├── .mvn/wrapper/          # Maven wrapper configuration
├── domain/                # domain POJOs, enums, invariants
├── application/           # use cases, ports, DTOs
├── infrastructure/        # JPA orm.xml, repositories, Flyway scripts, storage provider
├── api/                   # REST controllers, security config, DI wiring
├── openspec/              # OpenSpec specs and change proposals
└── tests/                 # unit + integration tests (JUnit 5, Testcontainers)
```

## Migrations

Migrations are Flyway SQL scripts, split per provider under Infrastructure:

- `db/migration/sqlite/` — SQLite migrations (default dev provider)
- `db/migration/sqlserver/` — SQL Server migrations
- `db/migration/postgres/` — PostgreSQL migrations

In development, Flyway applies automatically on application startup against the configured datasource. To run manually:

```bash
./mvnw -pl api spring-boot:run            # applies migrations then starts the app
```

## Quick Start

```bash
./mvnw clean install                      # build all modules
./mvnw -pl api spring-boot:run            # run the API (default SQLite, port 8080)
```

The API runs at `http://localhost:8080` (HTTP). In development, the OpenAPI document is at `http://localhost:8080/v3/api-docs` and Swagger UI at `http://localhost:8080/swagger-ui.html`.

## Configuration

Configured via `application.yml` (Spring profiles: `sqlite`, `sqlserver`, `postgres`):

- `spring.datasource.url` — database connection string
- `spring.flyway.locations` — migration script location per provider
- `deptflow.storage.provider` / `deptflow.storage.root-path` — storage backend (filesystem default)
- `deptflow.jwt.*` — JWT signing/expiry settings
- `logging`, `server.port`

## OpenSpec Workflow

This repository uses [OpenSpec](https://openspec.dev/) (spec-driven schema). Planning artifacts live under `openspec/changes/<change-name>/`:

- `proposal.md` — what & why
- `specs/<capability-path>/spec.md` — behavior contract (WHAT)
- `design.md` — technical approach (HOW)
- `tasks.md` — implementation checklist

The active change is `migrate-to-spring-boot`. Use `openspec status --change "<name>"` and `openspec instructions <artifact-id> --change "<name>" --json` to drive the workflow. Never hand-create a change directory — use `openspec new change "<name>"`.

## Testing

Test project `tests/` (JUnit 5 + AssertJ + Mockito) covers unit tests (hierarchy cycle detection, membership active/ended logic, approval state machine) and integration tests (tenant isolation, unique-constraint rejections, department-scoped document access, filesystem storage, full approval workflow) via `@DataJpaTest`/`@SpringBootTest` and Testcontainers for Postgres/SQL Server.

## Version Control Guidelines

- **NEVER** commit changes without user approval. Ask systematically for approval before committing.
- Commit messages should be clear and follow convention:
  - ai-tooling: AI agents, automation commands, workflows, or other AI-enabled developer tooling
  - feat: New feature
  - fix: Bug fix
  - docs: Documentation
  - style: Formatting
  - refactor: Code restructuring
  - test: Adding tests
  - chore: Maintenance tasks
- **NEVER** mention AI/Claude authorship in commit messages (no "Generated with Claude Code", "AI-assisted", etc.)

## Important Notes

- The default dev database is a local SQLite file (`deptflow.db`), which is gitignored.
- Auth endpoints (`/register`, `/login`) issue JWTs; protected endpoints require a `Bearer` token. Obtain one via `/login` first.
