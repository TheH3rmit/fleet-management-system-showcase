Fleet Management System

Step by step: from clone to production

Prerequisites
- Git
- Docker + Docker Compose (v2)
- Java 21 (for local backend run and tests)
- Node.js 20+ and npm (for frontend dev/test)

1) Clone the repository
`git clone https://github.com/TheH3rmit/fleet-management-system-showcase.git`
`cd fleet-management-system`

2) Local development (frontend + backend)

Start the database:
`docker compose --env-file .env.dev -f docker-compose.dev.yml up -d`

Backend (Spring Boot):
`cd fleet-management-system-backend`
`./mvnw spring-boot:run -Dspring-boot.run.profiles=dev`

Frontend (Angular):
`cd ../fleet-management-system-frontend`
`npm install`
`npm run start`

3) Run tests (optional)

Backend tests:
`cd fleet-management-system-backend`
`./mvnw verify`

Frontend unit tests:
`cd ../fleet-management-system-frontend`
`ng test --watch=false --browsers=ChromeHeadless`

Frontend E2E tests:
`npx playwright test`

4) Production deploy (single VPS, Docker)

On the server:
`git clone https://github.com/TheH3rmit/fleet-management-system-showcase.git`
`cd fleet-management-system`

Edit production environment:
- Copy `.env.example` to `.env.prod` and set strong values:
  - `JWT_SECRET` (required)
  - `DB_USER`, `DB_PASS`, `DB_NAME` as needed

Start containers:
`docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build`

Frontend is served by nginx on port 80.
Backend listens on port 8080.

Environment files
- `.env.dev` is for local development.
- `.env.prod` is for production.

Profiles
- Default: `application.yml`
- Dev: `application.yml` + `application-dev.yml`
- Prod: `application.yml` + `application-prod.yml`
- NoDocker: `application.yml` + `application-nodocker.yml`
