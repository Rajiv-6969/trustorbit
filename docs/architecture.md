# Architecture

TrustOrbit is one Java program plus a static website. Java does all the work and writes files; the
website only reads them, so there is no server to host.

```mermaid
flowchart LR
  subgraph MOSDAC [MOSDAC · SAC/ISRO]
    A[INSAT-3DR / 3DS<br/>LST + rainfall HDF5]
  end
  subgraph Java [Java program · Main]
    M1[M1 ingestion<br/>MosdacClient · IsroFileReader<br/>DataSource / CsvSource / JsonSource]
    M2[M2 preprocessing<br/>units · UTC · dedup · fill/QC · ranges · imputation]
    M3[M3 quality engine<br/>Weka IQR + EM/KMeans · drift · cross-satellite · neighbours]
    M4[M4 output<br/>SQLite · JSON export · alerts · OpenPDF]
  end
  CSV[(data/isro/*.csv)]
  DB[(SQLite)]
  WEB[website/public/data/*.json<br/>website/public/reports/*.pdf]
  SITE[Vite + React website<br/>on Vercel]

  A -- download, keep 1 file/day --> M1 --> CSV --> M2 --> M3 --> M4
  M4 --> DB
  M4 --> WEB --> SITE
```

## Commands → stages

| Command | Stages |
|---|---|
| `download` / `convert` | M1: MOSDAC → `data/isro/*.csv` |
| `run` | M1 read CSV → M2 clean → M3 score → M4 store + export |
| `score <file.csv>` | same as `run` for any CSV |
| `evaluate` | M3 on SYNTHETIC noise/gaps/drift → precision/recall |
| `report` | M4 PDF |

## Design decisions

- **Plain Java instead of Spring Boot / PostGIS / Thymeleaf.** The original proposal listed them; we
  simplified to fit the micro-project scope. SQLite replaces PostgreSQL/PostGIS (spatial work here
  is just nearest-pixel and nearest-station distance, done in Java). PostGIS and a REST API are
  future upgrades.
- **`DataSource` interface.** NASA or other sources can be added later as new implementations.
- **Raw files are transient.** HDF5 files are converted immediately and deleted: they are large and
  MOSDAC does not allow redistributing them.
