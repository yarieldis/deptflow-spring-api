# DeptFlow WebAPI

Backend services for the **DeptFlow** department workflow application — institutions, departments, the people working in them, and the (versioned) documents those people use, exposed as a multi-tenant REST API.

## Stack

- Java 25 (LTS), Spring Boot 4.1.x (Spring Framework 7)
- Spring MVC, Spring Security (JWT resource server)
- Spring Data JPA / Hibernate 7, Flyway (SQLite / SQL Server / PostgreSQL)

## Architecture

```
api  →  application  →  domain  ←  infrastructure
```

Maven modules enforce this rule: `domain` references nothing; `application` references `domain`; `infrastructure` references `domain` + `application`; `api` references `application` + `infrastructure`.

## Quick start

```bash
./mvnw clean install              # build all modules
./mvnw -pl api spring-boot:run    # run the API (default SQLite, port 8080)
```

- OpenAPI: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Docker

Run the app with a real database via Docker Compose:

```bash
docker-compose up --build                                  # app + PostgreSQL
DEPTFLOW_JWT_SECRET=... docker-compose up --build          # override the JWT secret
docker-compose -f docker-compose.sqlserver.yml up --build  # app + SQL Server
```

- App: `http://localhost:8080` (Swagger UI at `/swagger-ui.html`)
- PostgreSQL: `localhost:5432` (db/user/password: `deptflow`)
- SQL Server: `localhost:1433` (user `sa`, set `MSSQL_SA_PASSWORD`)

## Configuration

See `application.yml` (profiles: `sqlite`, `sqlserver`, `postgres`) and `AGENTS.md` for details.
