# Fleet Management System – README (EN)

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

## 2) Environment files (.env) – important

For security reasons, `.env.dev` and `.env.prod` files are **not stored in the repository**.
You must create them locally based on the `.env.example` file.

### 2.1 Create `.env.dev` (locally)

```bash
cp .env.example .env.dev
# then fill in values in .env.dev
```

### 2.2 Create `.env.prod` (on the server)

```bash
cp .env.example .env.prod
# then fill in values in .env.prod (use strong secrets/passwords!)
```

At minimum, for production you should set e.g.:
- `JWT_SECRET` (required),
- `DB_USER`, `DB_PASS`, `DB_NAME` (according to your DB setup).

> Note: never commit secrets to the repository. Add `.env.*` to `.gitignore`.

---

## 3) Local run (development mode)

### 3.1 Start the database and dependencies (Docker Compose)

From the project root directory:

```bash
docker compose --env-file .env.dev -f docker-compose.dev.yml up -d
```

This command starts services required for development (e.g., the database).

### 3.2 Start the backend (REST API)

```bash
cd fleet-management-system-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3.3 Start the frontend

In a new terminal:

```bash
cd fleet-management-system-frontend
npm install
npm run start
```

---

## 4) Tests (optional)

### 4.1 Backend – tests

```bash
cd fleet-management-system-backend
./mvnw verify
```

### 4.2 Frontend – unit tests (Angular)

```bash
cd fleet-management-system-frontend
ng test --watch=false --browsers=ChromeHeadless
```

### 4.3 Frontend – E2E tests (Playwright)

```bash
cd fleet-management-system-frontend
npx playwright test
```

---

## 5) Production deployment (Docker Compose)

### 5.1 On the server (VPS) – preparation

1. Clone the repository:

```bash
git clone https://github.com/TheH3rmit/fleet-management-system-showcase.git
cd fleet-management-system-showcase
```

2. Prepare the environment file:

```bash
cp .env.example .env.prod
# fill in values in .env.prod
```

### 5.2 Start containers (build + run)

From the project root directory:

```bash
sudo docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

### 5.3 Ports and access

- The frontend is served by **nginx** on port **80** (HTTP).
- The backend listens on port **8080** (REST API).

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

# Fleet Management System – README (PL)

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

## 2) Pliki środowiskowe (.env) – ważne

Z powodów bezpieczeństwa pliki `.env.dev` oraz `.env.prod` **nie są przechowywane w repozytorium**.
Należy je utworzyć lokalnie na podstawie pliku `.env.example`.

### 2.1 Utworzenie `.env.dev` (lokalnie)
```bash
cp .env.example .env.dev
# następnie uzupełnij wartości w .env.dev
```

### 2.2 Utworzenie `.env.prod` (na serwerze)
```bash
cp .env.example .env.prod
# następnie uzupełnij wartości w .env.prod (silne hasła/sekrety!)
```

Minimalnie dla produkcji należy ustawić m.in.:
- `JWT_SECRET` (wymagane),
- `DB_USER`, `DB_PASS`, `DB_NAME` (zgodnie z konfiguracją bazy).

> Uwaga: nigdy nie commituj sekretów do repozytorium. Dodaj `.env.*` do `.gitignore`.

---

## 3) Uruchomienie lokalne (tryb deweloperski)

### 3.1 Uruchomienie bazy danych i zależności (Docker Compose)

W katalogu głównym projektu:

```bash
docker compose --env-file .env.dev -f docker-compose.dev.yml up -d
```

To polecenie uruchomi usługi wymagane do działania aplikacji w trybie deweloperskim (np. bazę danych).

### 3.2 Uruchomienie backendu (REST API)

```bash
cd fleet-management-system-backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3.3 Uruchomienie frontendu

W nowym terminalu:

```bash
cd fleet-management-system-frontend
npm install
npm run start
```

---

## 4) Testy (opcjonalnie)

### 4.1 Backend – testy
```bash
cd fleet-management-system-backend
./mvnw verify
```

### 4.2 Frontend – testy jednostkowe (Angular)
```bash
cd fleet-management-system-frontend
ng test --watch=false --browsers=ChromeHeadless
```

### 4.3 Frontend – testy E2E (Playwright)
```bash
cd fleet-management-system-frontend
npx playwright test
```

---

## 5) Uruchomienie produkcyjne (Docker Compose)

### 5.1 Na serwerze (VPS) – przygotowanie
1. Klonowanie repozytorium:
```bash
git clone https://github.com/TheH3rmit/fleet-management-system-showcase.git
cd fleet-management-system-showcase
```

2. Przygotowanie pliku środowiskowego:
```bash
cp .env.example .env.prod
# uzupełnij wartości w .env.prod
```

### 5.2 Start kontenerów (build + uruchomienie)
W katalogu głównym projektu:

```bash
sudo docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

### 5.3 Porty i dostęp
- Frontend jest serwowany przez **nginx** na porcie **80** (HTTP).
- Backend nasłuchuje na porcie **8080** (REST API).

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
