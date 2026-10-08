# M1 · Ingestion

**Owner:** Abhi Rathod (1436)

## What it does
Pulls raw data in: `DataSourceAdapter` implementations for NASA POWER, FIRMS, EONET, ISRO CSVs and uploaded CSV/JSON files. Each record keeps source, product, fetch time and a hash of the raw payload.

## Why
Provenance first: if we can't say where a number came from, we can't say how much to trust it.

## Status
Phase 1: package created. Implementation lands in the phase listed in the project timeline.
