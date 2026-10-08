# M3 · Quality engine

**Owner:** Rajiv Siddharth (1420)

## What it does
Builds feature vectors and uses Weka (InterquartileRange, EM/SimpleKMeans) to score anomalies, detects slow sensor drift (Page-Hinkley/CUSUM), checks NASA vs ISRO agreement and EONET event context, then blends everything into a 0–100 reliability score with a one-line reason.

## Why
A score alone is not enough for decision makers – the breakdown explains *why* a record is trusted or flagged.

## Status
Phase 1: package created. Implementation lands in the phase listed in the project timeline.
