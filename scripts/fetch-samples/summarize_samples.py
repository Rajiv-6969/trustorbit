#!/usr/bin/env python3
"""Print a summary of the committed sample data (counts, fill values, date ranges).

    python scripts/fetch-samples/summarize_samples.py
"""
from __future__ import annotations

import collections
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SAMPLES = ROOT / "data" / "samples"


def power_summary(folder: str) -> None:
    print(f"NASA POWER daily [{folder}] (fill value -999 = missing)")
    print(f"{'station':8}{'days':>6}{'values':>8}{'fill(-999)':>12}  range")
    total_vals = total_fill = 0
    for f in sorted((SAMPLES / folder).glob("*.json")):
        doc = json.loads(f.read_text(encoding="utf-8"))
        fill = doc["header"]["fill_value"]
        params = doc["properties"]["parameter"]
        days = {d for series in params.values() for d in series}
        vals = sum(len(s) for s in params.values())
        fills = sum(1 for s in params.values() for v in s.values() if v == fill)
        fill_by_param = {p: sum(1 for v in s.values() if v == fill) for p, s in params.items()}
        fill_note = ", ".join(f"{p}:{n}" for p, n in fill_by_param.items() if n)
        total_vals += vals
        total_fill += fills
        print(f"{f.stem:8}{len(days):>6}{vals:>8}{fills:>12}  {min(days)}..{max(days)} {fill_note}")
    print(f"{'TOTAL':8}{'':>6}{total_vals:>8}{total_fill:>12}\n")


def eonet_summary() -> None:
    for f in sorted((SAMPLES / "eonet").glob("*.json")):
        events = json.loads(f.read_text(encoding="utf-8"))["events"]
        cats = collections.Counter(c["title"] for e in events for c in e["categories"])
        print(f"NASA EONET {f.name}: {len(events)} events -> {dict(cats)}")


def firms_summary() -> None:
    folder = SAMPLES / "firms"
    files = sorted(folder.glob("*.csv")) if folder.exists() else []
    if not files:
        print("NASA FIRMS: no sample yet (needs FIRMS_MAP_KEY)")
    for f in files:
        rows = f.read_text(encoding="utf-8").strip().splitlines()
        print(f"NASA FIRMS {f.name}: {len(rows) - 1} fire detections")


if __name__ == "__main__":
    power_summary("nasa-power")
    power_summary("nasa-power-recent")
    eonet_summary()
    firms_summary()
    isro = ROOT / "data" / "isro"
    n = len(list(isro.glob("*.csv"))) if isro.exists() else 0
    print(f"ISRO (MOSDAC/Bhoonidhi): {n} converted CSV files")
