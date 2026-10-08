# M2 · Preprocessing

**Owner:** Sameer Basha (1446)

## What it does
Cleans observations: Kelvin → °C and other unit normalisation, timestamps to UTC, de-duplication, fill-value and quality-flag handling, physical range checks per product, and missing-value filling (linear interpolation for short gaps, seasonal mean for longer ones). Filled values are marked `imputed=true`.

## Why
Machine learning on unclean data learns the noise. Every change is explicit and recorded, and filled values are never presented as real readings.

## Status
Package created in Phase 1. Implementation: Phase 2 (weeks 3–4).
