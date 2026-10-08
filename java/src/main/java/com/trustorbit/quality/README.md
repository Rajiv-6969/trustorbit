# M3 · Quality engine

**Owner:** Rajiv Siddharth (1420)

## What it does
Builds a feature vector per record (value, z-score, day-of-year sin/cos, rolling mean/std, neighbour difference, cross-satellite difference), uses Weka (`InterquartileRange`, `EM`/`SimpleKMeans`) for anomaly scores, detects sensor drift with Page-Hinkley/CUSUM, and blends everything into a 0–100 reliability score with a breakdown and a one-line reason. Includes the synthetic `evaluate` command.

## Why
A score alone is not enough for decision makers – the breakdown explains why a record is trusted or flagged.

## Status
Package created in Phase 1. Implementation: Phase 3 (weeks 5–6).
