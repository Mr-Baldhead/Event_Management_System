# Event Management System

Ett komplett eventhanteringssystem för svenska scoutorganisationer, byggt med Angular 19 och Jakarta EE.

## 📋 Innehåll

- [Funktioner](#funktioner)
- [Teknisk Stack](#teknisk-stack)
- [Snabbstart](#snabbstart)
- [Inloggningsuppgifter](#inloggningsuppgifter)
- [Projektstruktur](#projektstruktur)
- [API-dokumentation](#api-dokumentation)
- [Utveckling](#utveckling)
- [Felsökning](#felsökning)

---

## ✨ Funktioner

### Autentisering & Användare
- **SuperAdmin** - Hanterar administratörer
- **Admin** - Hanterar events och anmälningar
- BCrypt-krypterade lösenord
- Server-side sessioner (30 min timeout)
- Automatisk e-postnotifiering vid nya användare (via Resend)

### Eventhantering
- Skapa, redigera och arkivera events
- Dynamisk formulärbyggare med drag-and-drop
- Fördefinierade svenska fält (personnummer, mobilnummer, kårval)
- Villkorsfält (visa/dölj baserat på svar)
- Allergirapporter

### Anmälningar
- Publika anmälningsformulär
- Målsmansinformation för minderåriga
- Kår- och patrullhantering

---

## 🛠 Teknisk Stack

| Komponent | Teknologi |
|-----------|-----------|
| **Frontend** | Angular 19, Material Design, TypeScript |
| **Backend** | Jakarta EE 10, WildFly 34, JAX-RS |
| **Databas** | MySQL 8.3 |
| **Migrationer** | Flyway |
| **E-post** | Resend API |
| **Container** | Docker, Docker Compose |

---

## 🚀 Snabbstart

### Förutsättningar

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installerat
- Git

### 1. Klona projektet

```bash
git clone <repository-url>
cd event-management-system
```

### 2. Skapa miljöfil

Skapa en `.env` fil i projektets rot:

```bash
# .env
RESEND_API_KEY=re_din_api_nyckel_här
```

> **OBS:** E-postfunktionen är valfri. Systemet fungerar utan Resend API-nyckel, men då skickas inga e-postnotifieringar.

### 3. Starta systemet

```bash
docker-compose up --build
```

Vänta tills du ser:
```
eventmanager-wildfly | WFLYSRV0025: WildFly 34.0.1.Final started
```

### 4. Öppna applikationen

| Tjänst | URL |
|--------|-----|
| **Frontend** | http://localhost:4200 |
| **Backend API** | http://localhost:8080/api |
| **WildFly Admin** | http://localhost:9990 |

---

## 🔐 Inloggningsuppgifter

### SuperAdmin (första inloggning)

| Fält | Värde |
|------|-------|
| **E-post** | `superadmin@eventmanager.se` |
| **Lösenord** | `Admin123!` |

> **OBS:** Vid första inloggning måste du byta lösenord.

### Om du behöver återställa SuperAdmin-lösenordet

```bash
curl -X POST http://localhost:8080/api/setup/reset-superadmin
```

---

## 📁 Projektstruktur

```
event-management-system/
├── backend/                    # Jakarta EE backend
│   ├── src/main/java/com/eventmanager/
│   │   ├── config/            # JAX-RS konfiguration
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── entity/            # JPA Entities
│   │   ├── exception/         # Custom exceptions
│   │   ├── repository/        # Data access layer
│   │   ├── rest/              # REST endpoints
│   │   └── service/           # Business logic
│   ├── src/main/resources/
│   │   ├── db/migration/      # Flyway migrations
│   │   └── META-INF/          # persistence.xml
│   └── docker/                # WildFly konfiguration
├── frontend/                   # Angular 19 frontend
│   ├── src/app/
│   │   ├── core/              # Services, guards, interceptors
│   │   ├── features/          # Feature modules
│   │   │   ├── admin/         # SuperAdmin funktioner
│   │   │   ├── auth/          # Login, change-password
│   │   │   └── events/        # Event hantering
│   │   └── shared/            # Shared components, models
│   └── Dockerfile
├── nginx/                      # Nginx konfiguration
├── docker-compose.yml
├── .env                        # Miljövariabler (skapa själv)
└── README.md
```

---

## 📡 API-dokumentation

### Autentisering

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| POST | `/api/auth/login` | Logga in |
| POST | `/api/auth/logout` | Logga ut |
| GET | `/api/auth/me` | Hämta inloggad användare |
| POST | `/api/auth/change-password` | Byt lösenord |

### Användare (SuperAdmin)

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| GET | `/api/users` | Lista alla admins |
| POST | `/api/users` | Skapa ny admin |
| PUT | `/api/users/{id}` | Uppdatera admin |
| DELETE | `/api/users/{id}` | Ta bort admin |
| GET | `/api/users/counts` | Användarstatistik |

### Events

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| GET | `/api/events` | Lista events |
| POST | `/api/events` | Skapa event |
| GET | `/api/events/{id}` | Hämta event |
| PUT | `/api/events/{id}` | Uppdatera event |
| DELETE | `/api/events/{id}` | Ta bort event |

### Formulär

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| GET | `/api/events/{id}/form/fields` | Hämta formulärfält |
| POST | `/api/events/{id}/form/fields` | Spara formulärfält |

---

## 💻 Utveckling

### Stoppa alla containrar

```bash
docker-compose down
```

### Bygg om backend efter ändringar

```bash
docker-compose build --no-cache wildfly
docker-compose up
```

### Bygg om frontend efter ändringar

```bash
docker-compose build --no-cache frontend
docker-compose up
```

### Rensa allt och börja om

```bash
docker-compose down -v          # Tar bort volymer (databas)
docker system prune -f          # Rensar cache
docker-compose up --build
```

### Visa loggar

```bash
# Alla containrar
docker-compose logs -f

# Endast backend
docker-compose logs -f wildfly

# Endast frontend
docker-compose logs -f frontend
```

### Databas-access

```bash
docker exec -it eventmanager-mysql mysql -u eventuser -peventpassword eventmanager
```

---

## 🔧 Felsökning

### Problem: "Felaktig e-post eller lösenord"

Återställ SuperAdmin-lösenordet:
```bash
curl -X POST http://localhost:8080/api/setup/reset-superadmin
```

### Problem: Frontend visar 502 Bad Gateway

Backend har inte startat klart. Vänta 30-60 sekunder och ladda om sidan.

### Problem: "Could not initialize proxy - no session"

Detta är ett Hibernate lazy-loading problem. Se till att du använder senaste versionen av `AuthService.java`.

### Problem: E-post skickas inte

1. Kontrollera att `RESEND_API_KEY` finns i `.env`
2. Verifiera din domän på https://resend.com/domains
3. Kolla loggen: `docker-compose logs wildfly | grep -i email`

### Problem: Docker bygger inte om mina ändringar

```bash
docker-compose build --no-cache
docker-compose up
```

---

## 📊 Databasdiagram

Se `Databasdiagram.pdf` och `Classdiagram.png` i projektmappen för fullständig dokumentation av datamodellen.

---

## 👥 Roller

| Roll | Behörigheter |
|------|--------------|
| **SuperAdmin** | Hantera administratörer |
| **Admin** | Hantera events, formulär, anmälningar, allergier |

---

## 📧 Kontakt

Utvecklat som examensarbete 2026.

---

## 📝 Licens

Detta projekt är utvecklat för utbildningssyfte.
