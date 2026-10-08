# Common (shared)

**Owner:** Akash (1441)

## What it does
Shared building blocks every module uses: the `Station` entity and seeder, WGS84 geometry helpers (`Geo`), CORS configuration and the `/api/status` endpoint.

## Why
Keeping shared models in one place stops modules from depending on each other's internals and makes integration predictable.

## Status
Phase 1: package created. Implementation lands in the phase listed in the project timeline.
