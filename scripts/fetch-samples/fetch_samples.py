#!/usr/bin/env python3
"""Download the small, committed sample datasets used by TrustOrbit.

Only the Python standard library is used, so any teammate can run:

    python scripts/fetch-samples/fetch_samples.py

What it fetches (all free, no API key needed):
  * NASA POWER Daily API  -> data/samples/nasa-power/<STATION>.json
  * NASA EONET v3 events  -> data/samples/eonet/events-india-<years>.json
  * NASA FIRMS (optional) -> data/samples/firms/viirs-snpp-india-<days>d.csv
      only when the environment variable FIRMS_MAP_KEY is set.

Every file is written exactly as the API returned it (no edits), and a
manifest with the request URL, fetch time and SHA-256 is written to
data/samples/MANIFEST.json so the provenance of each file is auditable.
"""
from __future__ import annotations

import csv
import hashlib
import json
import os
import sys
import time
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SAMPLES = ROOT / "data" / "samples"
STATIONS = ROOT / "data" / "stations.csv"

POWER_START = "20230101"
POWER_END = "20241231"
POWER_PARAMS = "T2M,T2M_MAX,T2M_MIN,PRECTOTCORR,RH2M,ALLSKY_SFC_SW_DWN"
# India bounding box: west, south, east, north
INDIA_BBOX = (68.0, 6.0, 98.0, 37.5)


def get(url: str, retries: int = 3) -> bytes:
    """GET a URL with simple retry/backoff (be polite to free APIs)."""
    for attempt in range(1, retries + 1):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "TrustOrbit-student-project/0.1"})
            with urllib.request.urlopen(req, timeout=120) as resp:
                return resp.read()
        except Exception as exc:  # noqa: BLE001 - report and retry
            print(f"  attempt {attempt} failed: {exc}", file=sys.stderr)
            time.sleep(5 * attempt)
    raise RuntimeError(f"giving up on {url}")


def save(rel: str, body: bytes, url: str, manifest: list[dict]) -> None:
    path = SAMPLES / rel
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(body)
    manifest.append({
        "file": f"data/samples/{rel}",
        "url": url.replace(os.environ.get("FIRMS_MAP_KEY", "\0"), "{MAP_KEY}"),
        "fetched_at_utc": datetime.now(timezone.utc).isoformat(timespec="seconds"),
        "bytes": len(body),
        "sha256": hashlib.sha256(body).hexdigest(),
    })
    print(f"  saved {rel} ({len(body):,} bytes)")


def fetch_power(manifest: list[dict]) -> None:
    with STATIONS.open(newline="", encoding="utf-8") as fh:
        for row in csv.DictReader(fh):
            q = urllib.parse.urlencode({
                "parameters": POWER_PARAMS, "community": "AG",
                "latitude": row["latitude"], "longitude": row["longitude"],
                "start": POWER_START, "end": POWER_END, "format": "JSON",
            })
            url = f"https://power.larc.nasa.gov/api/temporal/daily/point?{q}"
            print(f"NASA POWER {row['station_id']} {row['name']}")
            save(f"nasa-power/{row['station_id']}.json", get(url), url, manifest)
            time.sleep(1)


def fetch_power_recent(manifest: list[dict], days: int = 120) -> None:
    """Recent window ending today: the newest days still carry -999 fill values
    (NASA POWER near-real-time lag), which is real missing data for M2 to catch."""
    end = datetime.now(timezone.utc).date()
    start = end.fromordinal(end.toordinal() - days)
    with STATIONS.open(newline="", encoding="utf-8") as fh:
        for row in csv.DictReader(fh):
            q = urllib.parse.urlencode({
                "parameters": POWER_PARAMS, "community": "AG",
                "latitude": row["latitude"], "longitude": row["longitude"],
                "start": start.strftime("%Y%m%d"), "end": end.strftime("%Y%m%d"), "format": "JSON",
            })
            url = f"https://power.larc.nasa.gov/api/temporal/daily/point?{q}"
            print(f"NASA POWER recent {row['station_id']} {start}..{end}")
            save(f"nasa-power-recent/{row['station_id']}.json", get(url), url, manifest)
            time.sleep(1)


def fetch_eonet(manifest: list[dict]) -> None:
    w, s, e, n = INDIA_BBOX
    q = urllib.parse.urlencode({
        "start": "2023-01-01", "end": "2024-12-31", "status": "all",
        "bbox": f"{w},{n},{e},{s}",  # EONET bbox order: minLon,maxLat,maxLon,minLat
    })
    url = f"https://eonet.gsfc.nasa.gov/api/v3/events?{q}"
    print("NASA EONET events (India bbox, 2023-2024)")
    save("eonet/events-india-2023-2024.json", get(url), url, manifest)


def fetch_firms(manifest: list[dict]) -> None:
    key = os.environ.get("FIRMS_MAP_KEY")
    if not key:
        print("FIRMS skipped: set FIRMS_MAP_KEY to fetch active-fire sample")
        return
    w, s, e, n = INDIA_BBOX
    url = (f"https://firms.modaps.eosdis.nasa.gov/api/area/csv/{key}/VIIRS_SNPP_NRT/"
           f"{w},{s},{e},{n}/5")
    print("NASA FIRMS VIIRS_SNPP_NRT, India, last 5 days")
    save("firms/viirs-snpp-india-5d.csv", get(url), url, manifest)


def main() -> None:
    manifest_path = SAMPLES / "MANIFEST.json"
    manifest: list[dict] = []
    if manifest_path.exists():
        manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    targets = set(sys.argv[1:]) or {"power", "recent", "eonet", "firms"}
    new: list[dict] = []
    if "power" in targets:
        fetch_power(new)
    if "recent" in targets:
        fetch_power_recent(new)
    if "eonet" in targets:
        fetch_eonet(new)
    if "firms" in targets:
        fetch_firms(new)
    replaced = {m["file"] for m in new}
    manifest = [m for m in manifest if m["file"] not in replaced] + new
    body = json.dumps(sorted(manifest, key=lambda m: m["file"]), indent=2) + "\n"
    manifest_path.write_bytes(body.encode("utf-8"))
    print(f"manifest: {len(manifest)} files")


if __name__ == "__main__":
    main()
