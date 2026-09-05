# Task Tracker API

A small Spring Boot REST API for managing tasks — built as a practice project to cover
exactly what a Java backend job description usually asks for: Java, Spring Boot, SQL/Postgres,
Git, RESTful APIs, JUnit/Mockito testing, and a SQL query written with a CTE.

## Stack

- Java 17, Spring Boot 3.3 (Web, Data JPA, Validation)
- PostgreSQL (runtime), H2 (test-only, so tests don't need Postgres running)
- JUnit 5 + Mockito for unit and slice tests
- Maven

## Project layout

```
src/main/java/com/example/tasktracker/
  model/        Task entity, TaskStatus enum
  repository/   TaskRepository (Spring Data JPA + one native CTE query)
  dto/          TaskRequest / TaskResponse / OverdueStatusSummary
  mapper/       TaskMapper (entity <-> DTO)
  service/      TaskService interface + impl (business logic)
  controller/   TaskController (REST endpoints)
  exception/    ResourceNotFoundException + a global @RestControllerAdvice handler
src/test/java/com/example/tasktracker/
  service/      TaskServiceImplTest      -> Mockito-based unit tests
  controller/   TaskControllerTest       -> @WebMvcTest + MockMvc slice tests
  repository/   TaskRepositoryTest       -> @DataJpaTest, exercises the CTE query
```

## 1. Set up PostgreSQL locally

```bash
# create the database once
createdb tasktracker
# or, inside psql:
# CREATE DATABASE tasktracker;
```

Update `src/main/resources/application.properties` if your username/password differ
from the defaults (`postgres` / `postgres`).

Hibernate is set to `ddl-auto=update`, so the `tasks` table is created automatically
on first run — no manual schema needed for this small project.

## 2. Run the app

```bash
mvn spring-boot:run
```

The API comes up on `http://localhost:8080`.

## 3. Try the endpoints

```bash
# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Write unit tests","description":"Cover the service layer","status":"TODO","dueDate":"2026-09-10"}'

# List all tasks
curl http://localhost:8080/api/tasks

# Filter by status
curl "http://localhost:8080/api/tasks?status=TODO"

# Get one task
curl http://localhost:8080/api/tasks/1

# Update a task
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Write unit tests","description":"Done","status":"DONE","dueDate":"2026-09-10"}'

# Delete a task
curl -X DELETE http://localhost:8080/api/tasks/1

# CTE-powered report: count of overdue, unfinished tasks grouped by status
curl http://localhost:8080/api/tasks/reports/overdue-summary
```

The last endpoint is backed by a native query in `TaskRepository` written with a
`WITH` clause (a CTE) — it first builds a temporary set of overdue, not-done tasks,
then aggregates that set by status. It's a small example, but it's the kind of query
pattern (CTE + aggregation) interviewers ask about directly.

## 4. Run the tests

```bash
mvn test
```

This runs three layers of tests, each testing a different thing:

- **`TaskServiceImplTest`** — pure Mockito unit tests. The repository and mapper are
  mocked, so these test only the service's business logic (e.g. throwing
  `ResourceNotFoundException` when an id doesn't exist) with no Spring context and no DB.
- **`TaskControllerTest`** — a `@WebMvcTest` slice test using `MockMvc`. The service is
  mocked; this verifies routing, status codes, validation errors, and JSON shape.
- **`TaskRepositoryTest`** — a `@DataJpaTest` against an in-memory H2 database in
  PostgreSQL compatibility mode, so the native CTE query itself gets exercised without
  needing a real Postgres instance for CI.

## Things you could extend this with (good talking points in an interview)

- Pagination (`Pageable`) on `GET /api/tasks`
- A `PATCH` endpoint for partial updates
- Flyway/Liquibase migrations instead of `ddl-auto=update`
- Basic auth or JWT on the endpoints
- A `docker-compose.yml` that spins up Postgres + the app together
