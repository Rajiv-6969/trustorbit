# Architecture

```mermaid
flowchart LR
  subgraph Sources
    P[NASA POWER<br/>daily API]
    F[NASA FIRMS<br/>active fires]
    E[NASA EONET<br/>events]
    I[ISRO MOSDAC<br/>INSAT-3D LST CSV]
    U[CSV / JSON upload]
  end
  subgraph Backend [Spring Boot backend · Java 21]
    M1[M1 Ingestion<br/>DataSourceAdapters]
    M2[M2 Preprocessing<br/>fill values · range checks · imputation]
    M3[M3 Quality engine<br/>Weka + drift + cross-source + event context]
    M4[M4 Dashboard API<br/>REST · alerts · PDF]
  end
  DB[(PostgreSQL + PostGIS)]
  UI[React + Leaflet UI<br/>Vercel]
  S[(snapshot/*.json)]

  P & F & E & I & U --> M1 --> M2 --> M3 --> DB
  DB --> M4 --> UI
  M4 -. export script .-> S -. fallback when API sleeps .-> UI
```

## Deployment

| Piece | Host | Notes |
|---|---|---|
| Front end | Vercel (Netlify config included) | static Vite build; falls back to `snapshot/*.json` |
| API | Render free web service (Docker) – Koyeb as alternative | sleeps when idle |
| Database | Neon or Supabase Postgres with `CREATE EXTENSION postgis` | |
