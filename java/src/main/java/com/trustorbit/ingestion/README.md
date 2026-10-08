# M1 · Ingestion

**Owner:** Abhi Rathod (1436)

## What it does
Turns raw satellite files into `Observation` records that the rest of the pipeline understands.

| Class | Purpose |
|---|---|
| `DataSource` | Interface every source implements – add NASA later without touching other modules |
| `IsroFileReader` | Opens INSAT HDF5/NetCDF files with NetCDF-Java and extracts the pixel nearest each station |
| `CsvSource`, `JsonSource` | Read observations from CSV/JSON (used by `run` and `score <file.csv>`) |
| `IsroProduct`, `IsroFileName` | Which MOSDAC products we use and how their file names are parsed |
| `MosdacClient`, `MosdacDownloader` | Search (no login) and download (MOSDAC login from `.env`) only the files we need |
| `ObservationCsv` | The CSV format stored in `data/isro/` |
| `DataSummary` | The per-product counts printed by `run` and `summary` |

## Why
Provenance first: every record keeps its source, satellite, product, file name and read-at time.
Fill values and invalid quality flags are recorded as **missing**, never as readings.

## Status
Phase 1: reader, downloader and CSV format done. Phase 2: more tests and edge cases.
