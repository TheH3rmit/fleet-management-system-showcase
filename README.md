# Fleet Management System - README (EN)

A web application supporting transport process management in a small transport company (TMS class).
The project consists of:
- a backend (REST API) and
- a frontend (web application),
  and the runtime/infrastructure setup is based on Docker and Docker Compose.

Repository: https://github.com/TheH3rmit/fleet-management-system-showcase

---

## Requirements

### Minimum (recommended for running locally / on a server)
- Git
- Docker
- Docker Compose (v2)

### Additional (if running without containers)
- Java 21 (backend)
- Node.js 20+ and npm (frontend)

---

## 1) Clone the repository

```bash
git clone https://github.com/TheH3rmit/fleet-management-system-showcase.git
cd fleet-management-system-showcase
```

---

## 2) Environment files (.env) - important

For production you must create `.env.prod` based on `.env.example`.
For local development you can use `.env.dev` (for Docker Compose) and `.env.dev.properties` (for Spring Boot).
For production you should use `.env.prod` (Docker Compose) and `.env.prod.properties` (Spring Boot).
The `.properties` variants exist so Spring Boot can read them directly.

### 2.1 Create `.env.dev` (locally)

```bash
cp .env.example .env.dev
cp .env.example.properties .env.dev.properties
# then fill in values in .env.dev / .env.dev.properties
```

### 2.2 Create `.env.prod` (on the server)

```bash
cp .env.example .env.prod
cp .env.example.properties .env.prod.properties
# then fill in values in .env.prod / .env.prod.properties (use strong secrets/passwords!)
```

At minimum, for production you should set e.g.:
- `JWT_SECRET` (required),
- `DB_USER`, `DB_PASS`, `DB_NAME` (according to your DB setup).

> Note: never commit secrets to the repository. Add `.env.*` to `.gitignore`.

---

## 3) Local run (development mode)

### 3.1 Start backend + database (Scenario A - recommended)

Backend starts Docker Compose (database) automatically via Spring Boot.

```bash
cd fleet-management-system-backend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Notes:
- Port `5432` must be free (or change it in `docker-compose.dev.yml`).
- `application-dev.yml` points to `../docker-compose.dev.yml` and `../.env.dev`.
- Docker Desktop (or Docker Engine) must be running.

Tip: If you want to override DB/JWT values for Spring Boot, create `.env.dev.properties` in the project root.

If you ever see `No PostgreSQL password found`, remove the old DB container/volume and retry:
```bash
docker compose -f docker-compose.dev.yml down -v
```

### 3.2 Optional: run DB manually + backend without Compose

Use when port `5432` is busy or when you do not want Spring Boot to manage Docker Compose.

```bash
docker compose --env-file .env.dev -f docker-compose.dev.yml up -d
cd fleet-management-system-backend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring.docker.compose.enabled=false"
```

### 3.3 Start the frontend

In a new terminal:

```bash
cd fleet-management-system-frontend
npm install
npm run start
```

Frontend URL: `http://localhost:4200`

---

## 4) Tests (optional)

### 4.1 Backend - tests

```bash
cd fleet-management-system-backend
./mvnw verify
```

### 4.2 Frontend - unit tests (Angular)

```bash
cd fleet-management-system-frontend
ng test --watch=false --browsers=ChromeHeadless
```

### 4.3 Frontend - E2E tests (Playwright)

```bash
cd fleet-management-system-frontend
npx playwright test
```

---

## 5) Production deployment (Docker Compose)

### 5.1 On the server (VPS) - preparation

1. Clone the repository:

```bash
git clone https://github.com/TheH3rmit/fleet-management-system-showcase.git
cd fleet-management-system-showcase
```

2. Prepare the environment file:

```bash
cp .env.example .env.prod
cp .env.example.properties .env.prod.properties
# fill in values in .env.prod / .env.prod.properties
```

### 5.2 Start containers (build + run)

From the project root directory:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

### 5.3 Ports and access

- The frontend is served by **nginx** on port **80** (HTTP).
- The backend listens on port **8080** (REST API).
- In the Docker setup, nginx acts as a reverse proxy and the API is available under `/api`.

---

## 6) Application profiles (Spring Boot)

Backend configuration is split into profiles:
- **Default**: `application.yml`
- **Dev**: `application.yml` + `application-dev.yml`
- **Prod**: `application.yml` + `application-prod.yml`
- **NoDocker**: `application.yml` + `application-nodocker.yml`

---

## 7) Demo environment (VPS)

The demo instance was deployed on a VPS with the following specs:
- 2 vCPU (AMD)
- 4 GB RAM
- 80 GB disk
- up to 20 TB transfer / month

---

## License / Author

Author: Jakub Pleban  
Repository: https://github.com/TheH3rmit/fleet-management-system-showcase

---

# Fleet Management System - README (PL)

Aplikacja webowa wspomagająca zarządzanie procesem transportowym w małej firmie transportowej (klasa TMS).
Projekt składa się z:
- backendu (REST API) oraz
- frontendu (aplikacja webowa),
  a infrastruktura uruchomieniowa jest przygotowana w oparciu o Docker oraz Docker Compose.

Repozytorium: https://github.com/TheH3rmit/fleet-management-system-showcase

---

## Wymagania

### Minimalne (zalecane do uruchomienia lokalnie / na serwerze)
- Git
- Docker
- Docker Compose (v2)

### Dodatkowe (jeśli uruchomienie następuje bez kontenerów)
- Java 21 (backend)
- Node.js 20+ oraz npm (frontend)

---

## 1) Klonowanie repozytorium

```bash
git clone https://github.com/TheH3rmit/fleet-management-system-showcase.git
cd fleet-management-system-showcase
```

---

## 2) Pliki środowiskowe (.env) - ważne

Z powodów bezpieczeństwa pliki `.env.dev` oraz `.env.prod` **nie są przechowywane w repozytorium**.
Pliki `.env.dev.properties` i `.env.prod.properties` również nie są commitowane.
Należy je utworzyć lokalnie na podstawie plików wzorcowych.

### 2.1 Utworzenie plików dev (lokalnie)
```bash
cp .env.example .env.dev
cp .env.example.properties .env.dev.properties
# następnie uzupełnij wartości w .env.dev / .env.dev.properties
```

### 2.2 Utworzenie plików prod (na serwerze)
```bash
cp .env.example .env.prod
cp .env.example.properties .env.prod.properties
# następnie uzupełnij wartości w .env.prod / .env.prod.properties (silne hasła/sekrety!)
```

Minimalnie dla produkcji należy ustawić m.in.:
- `JWT_SECRET` (wymagane),
- `DB_USER`, `DB_PASS`, `DB_NAME` (zgodnie z konfiguracją bazy).

> Uwaga: nigdy nie commituj sekretów do repozytorium. Dodaj `.env.*` do `.gitignore`.  
> Docker używa `.env.dev/.env.prod`, a Spring Boot używa `.env.dev.properties/.env.prod.properties`.

---

## 3) Uruchomienie lokalne (tryb deweloperski)

### 3.1 Uruchomienie backendu + bazy (Scenariusz A - rekomendowany)

Backend sam uruchamia Docker Compose (baza) przez Spring Boot.

```bash
cd fleet-management-system-backend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
```

Ważne:
- port `5432` musi być wolny (albo zmień port w `docker-compose.dev.yml`).
- `application-dev.yml` wskazuje `../docker-compose.dev.yml` i `../.env.dev`.
- Docker Desktop (lub Docker Engine) musi być uruchomiony.

Tip: Jeśli chcesz nadpisać wartości DB/JWT dla Spring Boota, utwórz `.env.dev.properties` w katalogu głównym.

Jeśli pojawi się błąd `No PostgreSQL password found`, usuń kontener/volumen bazy i uruchom ponownie:
```bash
docker compose -f docker-compose.dev.yml down -v
```

### 3.2 (Opcjonalnie) Ręczne uruchomienie bazy + backend bez Compose

Użyj, jeśli port `5432` jest zajęty albo nie chcesz, by Spring Boot odpalał Compose.

```bash
docker compose --env-file .env.dev -f docker-compose.dev.yml up -d
cd fleet-management-system-backend
./mvnw spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring.docker.compose.enabled=false"
```

### 3.3 Uruchomienie frontendu

W nowym terminalu:

```bash
cd fleet-management-system-frontend
npm install
npm run start
```

Adres frontendu: `http://localhost:4200`

---

## 4) Testy (opcjonalnie)

### 4.1 Backend - testy
```bash
cd fleet-management-system-backend
./mvnw verify
```

### 4.2 Frontend - testy jednostkowe (Angular)
```bash
cd fleet-management-system-frontend
ng test --watch=false --browsers=ChromeHeadless
```

### 4.3 Frontend - testy E2E (Playwright)
```bash
cd fleet-management-system-frontend
npx playwright test
```

---

## 5) Uruchomienie produkcyjne (Docker Compose)

### 5.1 Na serwerze (VPS) - przygotowanie
1. Klonowanie repozytorium:
```bash
git clone https://github.com/TheH3rmit/fleet-management-system-showcase.git
cd fleet-management-system-showcase
```

2. Przygotowanie plików środowiskowych:
```bash
cp .env.example .env.prod
cp .env.example.properties .env.prod.properties
# uzupełnij wartości w .env.prod / .env.prod.properties
```

### 5.2 Start kontenerów (build + uruchomienie)
W katalogu głównym projektu:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

### 5.3 Porty i dostęp
- Frontend jest serwowany przez **nginx** na porcie **80** (HTTP).
- Backend nasłuchuje na porcie **8080** (REST API).
- W wariancie dockerowym nginx działa jako reverse proxy i API jest dostępne pod `/api`.

---

## 6) Profile aplikacji (Spring Boot)

Konfiguracja backendu jest rozdzielona na profile:
- **Default**: `application.yml`
- **Dev**: `application.yml` + `application-dev.yml`
- **Prod**: `application.yml` + `application-prod.yml`
- **NoDocker**: `application.yml` + `application-nodocker.yml`

---

## 7) Środowisko demonstracyjne (VPS)

Aplikacja demonstracyjna była uruchamiana na serwerze VPS o parametrach:
- 2 vCPU (AMD)
- 4 GB RAM
- 80 GB dysk
- transfer do 20 TB / miesiąc

---

## Licencja / Autor

Autor: Jakub Pleban  
Repozytorium: https://github.com/TheH3rmit/fleet-management-system-showcase
