# Student Management API (Spring Boot)

A small **REST API** built with Spring Boot for managing students. It exposes full CRUD endpoints for a `students` table in PostgreSQL and protects them with **JWT authentication** (Spring Security + JJWT).

The app is stateless: you log in once to obtain a JWT, then send that token in the `Authorization` header on every following request.

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Prerequisites (what to install and from where)](#prerequisites-what-to-install-and-from-where)
- [Database setup (PostgreSQL)](#database-setup-postgresql)
- [Configuration](#configuration)
- [How to run](#how-to-run)
- [Authentication](#authentication)
- [API endpoints](#api-endpoints)
- [Example requests (curl)](#example-requests-curl)
- [Running the tests](#running-the-tests)
- [Project structure](#project-structure)
- [Troubleshooting](#troubleshooting)
- [Notes for production](#notes-for-production)

---

## Features

- Create, read, update and delete students (`id`, `name`, `level`).
- Filter students by level (`GET /api/students/level/{level}`).
- Repository helpers: find by exact level and find by name (case-insensitive, partial match).
- Spring Data JPA / Hibernate with automatic schema update on startup.
- Spring Security with stateless JWT authentication:
  - public login endpoint: `POST /api/auth/login`
  - all other endpoints require `Authorization: Bearer <token>`
- In-memory demo user (`admin` / `password`) — no user table required.

---

## Tech stack

| Component | Version / technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Web layer | Spring Web (Spring MVC, REST) |
| Persistence | Spring Data JPA, Hibernate |
| Database | PostgreSQL (JDBC driver, runtime scope) |
| Security | Spring Security, stateless sessions, CSRF disabled |
| JWT | `io.jsonwebtoken:jjwt` 0.12.5 (`api`, `impl`, `jackson`) |
| Build tool | Maven 3.9+ (Maven Wrapper configured for 3.9.16) |
| Tests | Spring Boot Test (JUnit 5, spring-security-test) |

---

## Prerequisites (what to install and from where)

1. **JDK 21** (the project sets `<java.version>21</java.version>`).
   - Download: [Eclipse Temurin (Adoptium) JDK 21](https://adoptium.net/temurin/releases/?version=21)
   - Or: [Oracle JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
   - Verify: `java -version` should show version 21.
   - ⚠️ If several JDKs are installed, make sure `JAVA_HOME` points to the JDK 21 installation. Maven uses `JAVA_HOME`, not necessarily the `java` found first on `PATH`.

2. **Maven 3.9+** — *optional*, because the Maven Wrapper (`mvnw` / `mvnw.cmd`) is included and downloads Maven 3.9.16 automatically (internet required on first run).
   - Download: [Apache Maven](https://maven.apache.org/download.cgi)
   - Verify: `mvn -version`

3. **PostgreSQL** (any recent version, e.g. 14+).
   - Download: [PostgreSQL official downloads](https://www.postgresql.org/download/)
   - Verify: `psql --version` or check that the service is running on port `5432`.

4. **A REST client** (optional): [Postman](https://www.postman.com/downloads/), [Insomnia](https://insomnia.rest/download), or just `curl` / PowerShell `Invoke-RestMethod`.

5. **Git** (only needed to clone/push this repository): [Git downloads](https://git-scm.com/downloads).

---

## Database setup (PostgreSQL)

The default configuration expects a database named `spring_db`, local PostgreSQL on port `5432`, user `postgres` with password `hama123`.

After installing PostgreSQL, create the database (for example in `psql` or pgAdmin):

```sql
CREATE DATABASE spring_db;
```

If your `postgres` user has a different password, either change it:

```sql
ALTER USER postgres WITH PASSWORD 'hama123';
```

…or, preferably, edit `src/main/resources/application.properties` to match your local setup.

> Hibernate is configured with `spring.jpa.hibernate.ddl-auto=update`, so the `students` table is created/updated automatically at startup — you do **not** need to create it manually.

---

## Configuration

All configuration lives in `src/main/resources/application.properties`:

```properties
spring.application.name=spring
spring.datasource.url=jdbc:postgresql://localhost:5432/spring_db
spring.datasource.username=postgres
spring.datasource.password=hama123
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

| Property | Meaning |
| --- | --- |
| `spring.datasource.url` | JDBC URL of the PostgreSQL database. |
| `spring.datasource.username` / `.password` | Database credentials. |
| `spring.jpa.hibernate.ddl-auto=update` | Hibernate updates the schema on startup (never use `update` blindly in production). |
| `spring.jpa.show-sql=true` | Prints generated SQL to the console (useful while developing). |

The server starts on the default port **8080** (`http://localhost:8080`). To change it, add `server.port=8081` to `application.properties` (or pass `--server.port=8081` when running).

---

## How to run

Open a terminal **in the project root** (the folder that contains `pom.xml`; in this workspace it is `D:\spring\spring`).

### Option A — Maven Wrapper (recommended, no separate Maven install)

Windows PowerShell / CMD:

```powershell
.\mvnw.cmd spring-boot:run
```

macOS / Linux:

```bash
./mvnw spring-boot:run
```

### Option B — installed Maven

```bash
mvn spring-boot:run
```

### Option C — build a JAR and run it

```bash
mvn clean package -DskipTests
java -jar target/spring-0.0.1-SNAPSHOT.jar
```

The API is ready when you see a log line like:

```
Started Application in X.XXX seconds
```

Quick health check (login is the only public endpoint):

```bash
curl -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"password\"}"
```

> On Windows PowerShell, use `Invoke-RestMethod` or `curl.exe`; the `curl` alias in PowerShell is `Invoke-WebRequest`.

---

## Authentication

1. `POST /api/auth/login` with `{"username":"admin","password":"password"}`.
2. The response is `{"token":"<JWT>"}`. The token is valid for **24 hours**.
3. Send it on every protected request:

```
Authorization: Bearer <token>
```

Details:

- Users are stored **in memory** (`InMemoryUserDetailsManager`) in `SecurityConfig`; the only user is `admin` / `password` with role `ADMIN`. The password uses the `{noop}` prefix, i.e. it is stored in plain text for demo purposes.
- CSRF is disabled and sessions are stateless (`SessionCreationPolicy.STATELESS`), which is typical for a JWT API.
- `JwtAuthenticationFilter` reads the `Authorization` header, validates the token with `JwtService`, and sets the Spring Security context.
- Tokens are signed with HS256 using a hard-coded 256-bit key in `JwtService`.

---

## API endpoints

Base URL: `http://localhost:8080`

| Method | Endpoint | Auth required | Description | Success response |
| --- | --- | --- | --- | --- |
| `POST` | `/api/auth/login` | No | Log in and get a JWT | `200 OK` + `{"token": "..."}` |
| `GET` | `/api/students` | Yes | List all students | `200 OK` + JSON array |
| `GET` | `/api/students/{id}` | Yes | Get one student by ID | `200 OK` + JSON object |
| `POST` | `/api/students` | Yes | Create a student | `201 Created` + JSON object |
| `PUT` | `/api/students/{id}` | Yes | Update a student by ID | `200 OK` + JSON object |
| `DELETE` | `/api/students/{id}` | Yes | Delete a student by ID | `204 No Content` |
| `GET` | `/api/students/level/{level}` | Yes | List students by level | `200 OK` + JSON array |

Student JSON shape:

```json
{
  "id": 1,
  "name": " Mohamed",
  "level": "L3"
}
```

`id` is generated by the database (`GenerationType.IDENTITY`); `name` and `level` are required (`nullable = false`).

Errors:
- Missing/wrong token → `401 Unauthorized`.
- Student not found (GET/PUT/DELETE with unknown id) → `500` produced by the `RuntimeException` thrown in `StudentService` (no dedicated exception handler is configured yet).

> `StudentRepo` also declares `findByNameContainingIgnoreCase(String name)`, but no endpoint currently exposes it.

---

## Example requests (curl)

### 1. Log in and save the token

```bash
curl -s -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"password\"}"
```

Copy the `token` value from the response and use it below.

### 2. Create a student

```bash
curl -X POST http://localhost:8080/api/students ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <YOUR_TOKEN>" ^
  -d "{\"name\":\"Ahmed\",\"level\":\"L2\"}"
```

### 3. List all students

```bash
curl http://localhost:8080/api/students ^
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### 4. Get one student

```bash
curl http://localhost:8080/api/students/1 ^
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### 5. Filter by level

```bash
curl http://localhost:8080/api/students/level/L2 ^
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### 6. Update a student

```bash
curl -X PUT http://localhost:8080/api/students/1 ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <YOUR_TOKEN>" ^
  -d "{\"name\":\"Ahmed B.\",\"level\":\"L3\"}"
```

### 7. Delete a student

```bash
curl -X DELETE http://localhost:8080/api/students/1 ^
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### PowerShell equivalent (login)

```powershell
$body = @{ username = 'admin'; password = 'password' } | ConvertTo-Json
$response = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' `
  -ContentType 'application/json' -Body $body
$token = $response.token
Invoke-RestMethod -Uri 'http://localhost:8080/api/students' `
  -Headers @{ Authorization = "Bearer $token" }
```

---

## Running the tests

```bash
mvn test
```

`ApplicationTests.contextLoads()` starts the full Spring context, which needs a reachable PostgreSQL database with the credentials from `application.properties`. **Start PostgreSQL and create `spring_db` first**, otherwise the test will fail with a datasource connection error.

To build without running tests (e.g. just to package):

```bash
mvn clean package -DskipTests
```

---

## Project structure

```
spring/
├── pom.xml                         # Maven dependencies & build (Spring Boot 4.1.1, Java 21)
├── mvnw, mvnw.cmd                  # Maven Wrapper scripts (Maven 3.9.16)
├── .mvn/wrapper/maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/hamabenhama/spring/
│   │   │   ├── Application.java              # @SpringBootApplication entry point
│   │   │   ├── Controller/
│   │   │   │   ├── AuthController.java       # POST /api/auth/login
│   │   │   │   └── StudentController.java    # /api/students CRUD endpoints
│   │   │   ├── Service/
│   │   │   │   └── StudentService.java       # Business logic
│   │   │   ├── Repository/
│   │   │   │   └── StudentRepo.java          # Spring Data JPA repository
│   │   │   ├── Entity/
│   │   │   │   └── StudentEntity.java        # JPA entity mapped to "students"
│   │   │   └── config/
│   │   │       ├── SecurityConfig.java       # Security rules, in-memory user, filter chain
│   │   │       ├── JwtAuthenticationFilter.java  # Reads & validates Bearer token
│   │   │       └── JwtService.java           # Token generation/validation (HS256, 24h)
│   │   └── resources/
│   │       └── application.properties        # Datasource + JPA settings
│   └── test/java/hamabenhama/spring/
│       └── ApplicationTests.java             # contextLoads smoke test
└── README.md
```

Architecture: `Controller → Service → Repository → Database`. Security runs in front of controllers as a servlet filter.

---

## Troubleshooting

| Problem | Likely cause / fix |
| --- | --- |
| `Unsupported class file major version` / `release version 21 not supported` | Maven is running on an older JDK. Set `JAVA_HOME` to JDK 21 and restart the terminal/IDE. |
| `Connection refused: localhost:5432` / `password authentication failed` | PostgreSQL is not running, `spring_db` does not exist, or credentials differ. Check the service and `application.properties`. |
| `Role "postgres" does not exist` | Adjust `spring.datasource.username` to a role that exists in your PostgreSQL installation. |
| `401 Unauthorized` on `/api/students` | You forgot the `Authorization: Bearer <token>` header, or the token expired (24h) — log in again. |
| `403 Forbidden` | CSRF/session misconfiguration is unlikely here; check that the token user still exists (`admin`) and that the path is correct. |
| `Web server failed to start. Port 8080 was in use.` | Stop the other process or set `server.port=8081` in `application.properties`. |
| `./mvnw` fails on first run | The wrapper downloads Maven 3.9.16 from Maven Central; check your internet connection/proxy, or use an installed Maven with `mvn`. |
| `mvn test` fails with a DB error | The context test needs a live database — start PostgreSQL and create `spring_db` (or use a test profile with an embedded DB). |

---

## Notes for production

- Replace the hard-coded JWT secret in `JwtService` with a value injected from an environment variable/secret manager (e.g. `${JWT_SECRET}`), and rotate it.
- Replace `{noop}password` and the in-memory user with a proper user store (e.g. a `users` table with BCrypt-encoded passwords).
- Never use `spring.jpa.hibernate.ddl-auto=update` in production; use Flyway/Liquibase migrations.
- Consider returning `404 Not Found` for missing students instead of a `500` (add a `@ControllerAdvice` exception handler).
- Keep `spring.jpa.show-sql=false` outside development.
