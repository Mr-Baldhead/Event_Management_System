# 🏕️ Event Management System

Ett modernt eventhanteringssystem. Systemet hanterar deltagarregistrering, allergirapportering och eventadministration.

## ✨ Funktioner

### Eventhantering
- Skapa, redigera och ta bort events
- Datum- och tidshantering med svensk formatering
- Platsinfo med adress, postnummer och ort
- Kapacitetshantering med automatisk beräkning av lediga platser

### Deltagarhantering
- Registrering av deltagare med fullständig kontaktinfo
- Stöd för målsman (vårdnadshavare) för minderåriga
- Kår/patrull-tillhörighet
- Sortering på efternamn, förnamn

### Allergirapportering
- Komplett allergiöversikt per event
- Gruppering per allergityp med antal drabbade
- Allvarlighetsnivåer (Kritisk, Hög, Medium, Låg)
- Export till Excel, PDF och CSV

### Export & Rapporter
- Deltagarlista till Excel
- Allergirapport till Excel/CSV
- PDF-utskrift direkt från webbläsaren

## 🛠️ Teknisk Stack

### Frontend
| Teknologi | Version | Beskrivning |
|-----------|---------|-------------|
| Angular | 19 | Frontend-ramverk med standalone components |
| Angular Material | 19 | UI-komponentbibliotek |
| TypeScript | 5.x | Typsäkert JavaScript |
| SCSS | - | CSS-preprocessor |

### Backend
| Teknologi | Version | Beskrivning |
|-----------|---------|-------------|
| Jakarta EE | 10 | Enterprise Java-plattform |
| WildFly | 34 | Applikationsserver |
| JPA/Hibernate | - | ORM för databasåtkomst |
| JAX-RS | - | REST API |

### Databas & Infrastruktur
| Teknologi | Version | Beskrivning |
|-----------|---------|-------------|
| MySQL | 8 | Relationsdatabas |
| Flyway | 10 | Databasmigrering |
| Docker | - | Containerisering |
| nginx | - | Reverse proxy |

## 📁 Projektstruktur

```
event-management-system/
├── frontend/                   # Angular-applikation
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/          # Tjänster, guards, interceptors
│   │   │   ├── features/      # Feature-moduler
│   │   │   │   └── events/    # Event-komponenter
│   │   │   └── shared/        # Delade komponenter
│   │   ├── assets/
│   │   └── styles/
│   └── Dockerfile
│
├── backend/                    # Jakarta EE-applikation
│   ├── src/main/java/com/eventmanager/
│   │   ├── dto/               # Data Transfer Objects
│   │   ├── entity/            # JPA-entiteter
│   │   ├── rest/              # REST-resurser
│   │   └── service/           # Affärslogik
│   ├── src/main/resources/
│   │   ├── db/migration/      # Flyway-migrationer
│   │   └── META-INF/
│   └── docker/
│       └── Dockerfile
│
├── docker-compose.yml          # Docker Compose-konfiguration
└── README.md
```

## 🚀 Installation

### Förutsättningar
- Docker och Docker Compose
- Git

### Starta projektet

```bash
# Klona repositoryt
git clone <repository-url>
cd event-management-system

# Starta alla tjänster
docker compose up -d

# Kör databasmigrationer
docker compose run --rm flyway migrate

# Kontrollera att allt körs
docker compose ps
```

### Åtkomst
| Tjänst | URL |
|--------|-----|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080/api |
| MySQL | localhost:3306 |

## 📡 API-dokumentation

### Events

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| GET | `/api/events` | Hämta alla events |
| GET | `/api/events/{id}` | Hämta specifikt event |
| POST | `/api/events` | Skapa nytt event |
| PUT | `/api/events/{id}` | Uppdatera event |
| DELETE | `/api/events/{id}` | Ta bort event |

### Registreringar

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| GET | `/api/events/{id}/registrations` | Hämta deltagare för event |
| DELETE | `/api/events/{id}/registrations/{regId}` | Ta bort deltagare |
| GET | `/api/events/{id}/registrations/excel` | Exportera till Excel |

### Allergirapporter

| Metod | Endpoint | Beskrivning |
|-------|----------|-------------|
| GET | `/api/events/{id}/allergy-report` | Hämta allergirapport |
| GET | `/api/events/{id}/allergy-report/excel` | Exportera till Excel |
| GET | `/api/events/{id}/allergy-report/csv` | Exportera till CSV |

## 🎨 Färgpalett

Projektet använder en konsekvent färgskala:

### Primär (Blå)
| Färg | Hex | Användning |
|------|-----|------------|
| Ljusblå | `#ECF1FA` | Bakgrunder |
| Mellablå | `#BFD3EE` | Borders, hover |
| Primärblå | `#5A88B7` | Knappar |
| Mörkblå | `#29415A` | Text, rubriker |

### Accent (Teal)
| Färg | Hex | Användning |
|------|-----|------------|
| Ljus teal | `#B6FEF5` | Highlights |
| Primär teal | `#1D968C` | Länkar, ikoner |
| Mörk teal | `#094944` | Hover-states |

### Grå
| Färg | Hex | Användning |
|------|-----|------------|
| Ljusgrå | `#F0F1F1` | Bakgrunder |
| Mellangrå | `#82848C` | Sekundär text |
| Mörkgrå | `#1F2023` | Primär text |

## 🔧 Utveckling

### Frontend-utveckling

```bash
cd frontend
npm install
npm start
```

### Backend-utveckling

```bash
cd backend
mvn clean package
```

### Bygga Docker-images

```bash
# Bygg allt
docker compose build --no-cache

# Bygg specifik tjänst
docker compose build --no-cache frontend
docker compose build --no-cache wildfly
```

### Databashantering

```bash
# Kör migrationer
docker compose run --rm flyway migrate

# Reparera migrationshistorik
docker compose run --rm flyway repair

# Visa migrationsstatus
docker compose run --rm flyway info
```

## 📋 Databasschema

### Huvudtabeller
- `events` - Eventinformation
- `participants` - Deltagardata
- `registrations` - Koppling event-deltagare
- `patrols` - Kårer/patruller
- `allergens` - Allergityper
- `participant_allergens` - Deltagarallergier

## 🔐 Säkerhet

- Lösenord hashas med säker algoritm
- DTO-mönster för att undvika exponering av entiteter
- CORS-konfiguration för frontend-åtkomst
- Prepared statements för SQL-frågor

## 📝 Kodkonventioner

- Kommentarer på engelska: `// This is a comment`
- Svenska etiketter i UI
- TypeScript strict mode
- Angular standalone components
- Signals för reaktiv state-hantering


## 📄 Licens

Detta projekt är utvecklat som ett examensarbete 2026.
