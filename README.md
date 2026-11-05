# user-service

Accounts, authentication and loyalty service of the ShopLoc platform.

## Status

Skeleton only: the Spring Boot project builds and exposes the Actuator health endpoint. Business features will be added in later iterations.

## Tech stack

- Java 21
- Spring Boot (Web, Actuator)
- Maven (wrapper included)

## Prerequisites

- JDK 21
- Git

## Build and test

```bash
./mvnw -B verify
```

## Run locally

```bash
./mvnw spring-boot:run
```

The service listens on port 8080 by default. Check that it is up:

```bash
curl http://localhost:8080/actuator/health
```

Expected response: `{"status":"UP"}`

## Branching and commits

- `main`: release branch only.
- `develop`: integration branch (default).
- `feature/GLOP-xx-short-description`: one branch per Jira ticket.
- Commit messages follow Conventional Commits and include the ticket, for example `feat(user): add health check [GLOP-12]`.

All changes go through a pull request with one approval.

## Part of ShopLoc

This repository is one microservice of the ShopLoc platform, under the `shoploc-athena` organization.
