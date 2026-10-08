# Data sources

All data used by TrustOrbit is free and open. Small **real** samples are committed so the project
runs without keys or logins. `data/samples/MANIFEST.json` records the exact request URL, fetch
time (UTC) and SHA-256 of every committed file. Re-download with:

```bash
python scripts/fetch-samples/fetch_samples.py            # power, recent, eonet (+ firms if FIRMS_MAP_KEY set)
python scripts/fetch-samples/summarize_samples.py
```

Region: India, 10 sample stations in [`data/stations.csv`](data/stations.csv)
(Bengaluru, Chennai, Delhi, Mumbai, Kolkata, Guwahati, Jaipur, Bhopal, Thiruvananthapuram, Leh).

## NASA (used live by the back end, samples committed)

### 1. NASA POWER – Daily point API
- **Endpoint:** `https://power.larc.nasa.gov/api/temporal/daily/point` (no key). Verified 2026-10-08,
  API v2.10.0.
- **Parameters:** `T2M, T2M_MAX, T2M_MIN` (°C), `PRECTOTCORR` (mm/day), `RH2M` (%),
  `ALLSKY_SFC_SW_DWN` (MJ/m²/day), community `AG`.
- **Missing data:** the response header declares `fill_value: -999.0`. TrustOrbit treats -999 as
  *missing*, never as a reading.
- **Committed files:**
  - `data/samples/nasa-power/<STATION>.json` – 2023-01-01 → 2024-12-31 (731 days × 6 params × 10
    stations). Historical, contains no fill values.
  - `data/samples/nasa-power-recent/<STATION>.json` – last ~120 days up to the fetch date. The
    newest 3–5 days contain genuine `-999` fill values (near-real-time processing lag) – real gaps
    for M2 to catch.
- **Terms / attribution:** "These data were obtained from the NASA Langley Research Center (LaRC)
  POWER Project funded through the NASA Earth Science/Applied Science Program."
  <https://power.larc.nasa.gov/docs/referencing/>

### 2. NASA FIRMS – active fires (VIIRS S-NPP 375 m)
- **Live API (needs free MAP_KEY):**
  `https://firms.modaps.eosdis.nasa.gov/api/area/csv/{MAP_KEY}/VIIRS_SNPP_NRT/{west,south,east,north}/{days}`.
  Get a key at <https://firms.modaps.eosdis.nasa.gov/api/map_key/> and put it in `.env` as
  `FIRMS_MAP_KEY` (never commit it).
- **Committed sample (no key needed):** `data/samples/firms/viirs-snpp-india-2024-subset.csv` –
  rows copied unchanged from the public country archive
  `https://firms.modaps.eosdis.nasa.gov/data/country/viirs-snpp/2024/viirs-snpp_2024_India.csv`,
  filtered to acquisition dates 2024-04-01..03 and 2024-11-01..07 (24,226 detections; keeps the
  file under 2 MB).
- **Quality signals used:** `confidence` (l/n/h), `frp` (fire radiative power, MW), `bright_ti4` (K).
- **Attribution:** "We acknowledge the use of data and imagery from LANCE FIRMS operated by
  NASA's Earth Science Data and Information System (ESDIS) with funding provided by NASA
  Headquarters." <https://www.earthdata.nasa.gov/data/tools/firms>

### 3. NASA EONET v3 – natural events
- **Endpoint:** `https://eonet.gsfc.nasa.gov/api/v3/events` (no key). Note the `bbox` order is
  `minLon,maxLat,maxLon,minLat`.
- **Committed file:** `data/samples/eonet/events-india-2023-2024.json` – all events (open + closed)
  in the India bounding box, 2023–2024: 17 events (12 severe storms, 3 wildfires, 2 volcanoes).
- **Use:** context – an extreme reading near a real event is penalised less ("real event vs bad
  sensor").
- **Attribution:** NASA Earth Observatory Natural Event Tracker (EONET). <https://eonet.gsfc.nasa.gov/>

## ISRO (registration required – not yet downloaded)

### 4. MOSDAC – Space Applications Centre (INSAT-3D / 3DR / 3DS products)
- **Status (checked 2026-10-08):** an official download API exists – the Python tool `mdapi`
  (<https://www.mosdac.gov.in/software/mdapi.zip>, manual at <https://mosdac.gov.in/downloadapi-manual>).
  **Searching needs no login; downloading needs an approved MOSDAC account**
  (<https://mosdac.gov.in/signup/>). Dataset IDs are listed at
  <https://mosdac.gov.in/catalog-app/satellite.php>.
- **Plan:** a team member registers, downloads a few days of INSAT-3D/3DR Land Surface
  Temperature for India, and runs `scripts/isro-convert/` to produce small point CSVs in
  `data/isro/` (columns: source, product, timestamp_utc, lat, lon, value, units). Raw HDF5 files
  are never committed.
- **Attribution:** "Data courtesy MOSDAC, Space Applications Centre, ISRO."

### 5. Bhoonidhi – NRSC EO data hub
- <https://bhoonidhi.nrsc.gov.in> (Resourcesat, Cartosat, EOS). Requires registration; documented
  as future work.

### 6. Bhuvan – NRSC geoportal
- A public OGC WMS endpoint responds without login:
  `https://bhuvan-vec1.nrsc.gov.in/bhuvan/wms?service=WMS&request=GetCapabilities` (verified
  2026-10-08). Planned as an optional map overlay, attributed "© NRSC/ISRO Bhuvan".

## Synthetic data

Any synthetic data (e.g. the evaluation harness that injects noise, gaps and drift) is generated
in code at test time, clearly labelled **SYNTHETIC**, and never mixed into the real samples.
