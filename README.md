# TrustOrbit

**AI-Based Space Intelligence & Geospatial Data Reliability Management System**
(college Java micro project)

Climate tracking, disaster monitoring and resource management depend on satellite data, but raw
data contains noise, gaps and gradual sensor drift that go unnoticed. TrustOrbit is a Java program
that reads **ISRO** INSAT-3DR and INSAT-3DS satellite products, cleans them, uses **Weka** to detect
anomalies, and gives **every record a 0–100 reliability score with a plain-English reason**. A
static website shows trustworthy and flagged data on a map, lists alerts and offers PDF reports.

| Score | Band |
|---|---|
| 85–100 | Verified |
| 60–84 | Acceptable |
| 35–59 | Questionable |
| 0–34 | Unreliable |

> **Status:** Phase 1 (data & setup). The live website link is added in Phase 5.

## Run it locally (3 commands)

Needs only **JDK 21** and **Node 22 LTS** – the Maven wrapper downloads Maven for you.

```bash
git clone https://github.com/Rajiv-6969/trustorbit.git && cd trustorbit/java
./mvnw -q exec:java -Dexec.args="run"            # Windows: .\mvnw.cmd -q exec:java "-Dexec.args=run"
cd ../website && npm install && npm run dev       # http://localhost:5173
```

### Commands

| Command | What it does |
|---|---|
| `run` | Full pipeline on `data/isro/*.csv` (ingest → clean → score → store → export) |
| `summary` | Counts per product/satellite: records, values, missing, date range |
| `search <from> <to>` | Lists the MOSDAC files a download would fetch (no login) |
| `download <from> <to>` | Downloads only the needed MOSDAC files, converts them, deletes the raw files (MOSDAC login in `.env`) |
| `convert <file-or-folder>` | Converts already-downloaded `.h5`/`.nc` files into `data/isro/*.csv` |
| `score <file.csv>` · `evaluate` · `report <region> <from> <to>` | Phases 3–4 |

On Windows PowerShell put the whole `-Dexec.args=...` in quotes, e.g.
`.\mvnw.cmd -q exec:java "-Dexec.args=search 2025-10-01 2025-11-30"`.

## Tech

| Part | Tech |
|---|---|
| Program | Java 21, Maven (single module, wrapper included), plain packages – no framework |
| Satellite files | NetCDF-Java (`cdm-core`) for HDF5/NetCDF |
| Machine learning | Weka 3.8 |
| Storage | SQLite (`sqlite-jdbc`, one file, nothing to install) |
| Reports | OpenPDF |
| JSON / tests | Jackson, JUnit 5 |
| Website | Vite + React + TypeScript, react-leaflet – static, hosted on Vercel (Netlify config included) |

**Scope note.** The original proposal listed Spring Boot, PostgreSQL/PostGIS and Thymeleaf. To
fit the micro-project scope we simplified to a plain Java program with SQLite and OpenPDF, and a
static website that reads JSON exported by Java – so there is no server to host. PostGIS (spatial
queries) and a REST API are listed as future upgrades.

## Repository layout

```
trustorbit/
├─ java/                      Maven project (package com.trustorbit)
│  └─ src/main/java/com/trustorbit/
│     ├─ Main.java            pipeline + commands – Akash
│     ├─ model/               shared Station/Observation – Akash
│     ├─ ingestion/           M1 – Abhi Rathod
│     ├─ preprocessing/       M2 – Sameer Basha
│     ├─ quality/             M3 – Rajiv Siddharth
│     └─ output/              M4 – Saikirantejas GS
├─ website/                   Vite + React + TS – Saikirantejas GS
├─ data/                      stations.csv, isro/ (converted station CSVs)
├─ docs/                      architecture, report notes
└─ README.md  CONTRIBUTING.md  DATA_SOURCES.md
```

See [docs/architecture.md](docs/architecture.md) and [CONTRIBUTING.md](CONTRIBUTING.md).

## Team

| Member | Roll no. | Role | Owns module |
|---|---|---|---|
| Akash | 1441 | Team lead & integration | Project setup, `Main` pipeline, CI, final testing |
| Abhi Rathod | 1436 | Data ingestion | M1: ISRO dataset selection, file reading/conversion |
| Sameer Basha | 1446 | Data preprocessing | M2: cleaning, normalisation, missing-value handling |
| Rajiv Siddharth | 1420 | AI / ML engine | M3: Weka anomaly detection, 0–100 reliability score |
| Saikirantejas GS | 1464 | Dashboard & reports | M4: data store, JSON export, PDF reports, website |

## Data credit

Data Source MOSDAC/SAC/ISRO. https://mosdac.gov.in – details and DOIs in
[DATA_SOURCES.md](DATA_SOURCES.md).
