# 🛰️ TrustOrbit

> *"Not all satellite data is created equal."*

**Official title:** AI-Based Space Intelligence & Geospatial Data Reliability Management System
(College Java micro project)

TrustOrbit ingests open NASA and ISRO satellite datasets, cleans them, uses machine learning
(Weka) to detect anomalies, and gives **every record a 0–100 reliability score with a
plain-English reason**. Trustworthy vs flagged data is shown on an interactive map of India,
with alerts and downloadable PDF quality reports.

| Score | Band | |
|---|---|---|
| 85–100 | Rock solid | 🟢 |
| 60–84 | Mostly honest | 🟡 |
| 35–59 | Kinda sus | 🟠 |
| 0–34 | Cosmic noise | 🔴 |

> **Status:** Phase 1 (data & setup) complete. Live URLs will be added in Phase 5.

## Run it locally (3 commands)

```bash
git clone https://github.com/Rajiv-6969/trustorbit.git && cd trustorbit
docker compose up --build            # PostGIS + Spring Boot API on :8080 (stations seeded)
cd frontend && npm install && npm run dev   # UI on http://localhost:5173
```

API docs: <http://localhost:8080/swagger-ui.html> · status: <http://localhost:8080/api/status>

No API keys or ISRO logins are needed — small real sample datasets are committed under
[`data/samples/`](data/samples) (see [DATA_SOURCES.md](DATA_SOURCES.md)).

## Tech stack

| Layer | Tech |
|---|---|
| Back end | Java 21, Spring Boot 3.5, Maven |
| Machine learning | Weka 3.8 (`weka-stable`) |
| Storage | PostgreSQL 16 + PostGIS 3.4, Hibernate Spatial (JTS), Flyway |
| Reports | Thymeleaf + OpenHTMLtoPDF |
| Front end | Vite + React + TypeScript, Leaflet |
| Hosting | Vercel (UI, Netlify config included) · Render (API, Docker) · Neon/Supabase (Postgres + PostGIS) |

## Repository layout

```
trustorbit/
├─ backend/                 Spring Boot (package com.trustorbit)
│  └─ src/main/java/com/trustorbit/
│     ├─ ingestion/         M1 – Abhi Rathod
│     ├─ preprocessing/     M2 – Sameer Basha
│     ├─ quality/           M3 – Rajiv Siddharth
│     ├─ dashboard/         M4 – Saikirantejas GS
│     └─ common/            shared models/config – Akash
├─ frontend/                Vite + React + TS – Saikirantejas GS
├─ data/                    stations.csv, samples/ (NASA), isro/ (converted ISRO CSVs)
├─ scripts/                 sample fetcher, ISRO converter, snapshot export
├─ docs/                    architecture, API docs, report material
└─ docker-compose.yml
```

See [docs/architecture.md](docs/architecture.md) for the pipeline diagram and
[CONTRIBUTING.md](CONTRIBUTING.md) for how we work as a team.

## 👩‍🚀 Crew

| Member | Roll no. | Role | Owns module |
|---|---|---|---|
| Akash | 1441 | Team lead & integration | Project setup, integration, CI, final testing |
| Abhi Rathod | 1436 | Data ingestion | M1: NASA/ISRO dataset selection, CSV/JSON/API import |
| Sameer Basha | 1446 | Data preprocessing | M2: cleaning, normalisation, missing-value handling |
| Rajiv Siddharth | 1420 | AI / ML engine | M3: Weka anomaly detection, 0–100 reliability score |
| Saikirantejas GS | 1464 | Dashboard & reports | M4: UI, PostGIS storage, map view, PDF export |

## Data credits

NASA POWER, NASA FIRMS, NASA EONET, ISRO MOSDAC, NRSC Bhoonidhi & Bhuvan — full attribution in
[DATA_SOURCES.md](DATA_SOURCES.md).
