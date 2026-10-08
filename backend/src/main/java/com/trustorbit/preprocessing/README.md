# M2 · Preprocessing

**Owner:** Sameer Basha (1446)

## What it does
Cleans the raw records: UTC timestamps, unit normalisation, de-duplication, fill-value (-999) detection, physical range checks and gap imputation (linear for short gaps, seasonal mean for long ones). Imputed values are flagged `imputed=true`.

## Why
Machine learning on dirty data learns the dirt. Cleaning is explicit and every change is recorded.

## Status
Phase 1: package created. Implementation lands in the phase listed in the project timeline.
