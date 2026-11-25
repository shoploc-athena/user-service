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

## Merchant Registration

`POST /api/users/merchants` (public endpoint) with `{ "email": "...", "password": "...", "firstName": "...", "lastName": "...", "storeName": "...", "storeAddress": "..." }`.

**Request validation:**
- `email`: required, valid email format, max 255 chars. Lowercased before storage.
- `password`: required, 12–72 chars. UTF-8 encoding must not exceed 72 bytes.
- `firstName`, `lastName`: required, max 100 chars each.
- `storeName`: required, max 150 chars.
- `storeAddress`: required, max 255 chars.

**Responses:**
- `201`: `{ requestId, status: "PENDING" }` — merchant account created, pending association approval.
- `400`: validation error (see `errors` array with field names and messages).
- `409`: email already exists, `code` = `EMAIL_ALREADY_EXISTS`.

The email is lowercased on both registration and login for consistency. Invalid email formats (e.g., with leading/trailing spaces) are rejected at validation.

## Authentication

`POST /api/users/auth/login` with `{ "email": "...", "password": "..." }`.

* `200`: `{ accessToken, tokenType: "Bearer", expiresIn }` (`expiresIn` in seconds)
* `401`: wrong email or password (same answer in both cases)
* `403`: account not allowed to log in, with `code` = `ACCOUNT_PENDING` (for a merchant: also
  `merchantRequestStatus` and `refusalReason`) or `ACCOUNT_DISABLED`

The token is signed with HS256 and carries `sub`, `city`, `role`, `permissions`, `iat`, `exp`.

Required environment variables (the service refuses to start without them):

| Variable | Description |
|---|---|
| `JWT_SECRET` | Secret shared by all services, at least 32 bytes. Never commit it. |
| `CITY` | City of this instance, written in every token and checked on each request. |
| `JWT_EXPIRATION_MINUTES` | Optional, 15 by default. |

## Branching and commits

- `main`: release branch only.
- `develop`: integration branch (default).
- `feature/GLOP-xx-short-description`: one branch per Jira ticket.
- Commit messages follow Conventional Commits and include the ticket, for example `feat(user): add health check [GLOP-12]`.

All changes go through a pull request with one approval.

## Part of ShopLoc

This repository is one microservice of the ShopLoc platform, under the `shoploc-athena` organization.
