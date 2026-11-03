# DermaWise

Expert system for skin condition diagnostics and dermatological therapy recommendations, built with Drools.
Project for the course "Sistemi bazirani na znanju" (SBNZ), FTN Novi Sad.

The project proposal is in `DermaWise PP - final.pdf`.

## Modules

| Module | Package | Contents |
|---|---|---|
| `model` | `com.ftn.sbnz.model` | facts, enums and DTOs shared by kjar and service |
| `kjar` | `com.ftn.sbnz.kjar` | DRL rules, templates and rule tests |
| `service` | `com.ftn.sbnz.service` | Spring Boot application (REST API), the only module that runs |

`kjar` depends on `model`, `service` depends on `kjar` and `model`.
The root `pom.xml` builds them in that order.

## Requirements

- JDK 17
- Docker with Docker Compose

Maven does not have to be installed, the project uses the Maven wrapper.

## Build

```
./mvnw clean install
```

This builds `model`, then `kjar`, then `service`, and runs all tests.

## Run

Start the database:

```
docker compose up -d
```

PostgreSQL 16 is exposed on host port `5433` by default, so it does not clash with a local installation.
The port, database name, user and password can be changed with `POSTGRES_PORT`, `POSTGRES_DB`, `POSTGRES_USER` and `POSTGRES_PASSWORD` (for example in a `.env` file).

Start the service:

```
./mvnw -pl service spring-boot:run
```

The service runs on `http://localhost:8080`.

Stop the database:

```
docker compose down
```
