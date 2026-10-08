# Data sources

TrustOrbit currently uses **ISRO data only**. All reading goes through the `DataSource` interface,
so other sources (e.g. NASA) can be added later without changing the rest of the program.

Region: India, 10 stations in [`data/stations.csv`](data/stations.csv) – Bengaluru, Chennai, Delhi,
Mumbai, Kolkata, Guwahati, Jaipur, Bhopal, Thiruvananthapuram, Leh.

## 1. MOSDAC – Space Applications Centre, ISRO (main source)

<https://mosdac.gov.in> · checked 2026-10-08

### Products used (IDs verified with the MOSDAC search API)

| Dataset ID | Satellite | Product | Frequency | What we keep | DOI |
|---|---|---|---|---|---|
| `3RIMG_L2B_LST` | INSAT-3DR | Land surface temperature | half-hourly | the 06:15 UTC file each day | [10.19038/SAC/10/3RIMG_L2B_LST](https://doi.org/10.19038/SAC/10/3RIMG_L2B_LST) |
| `3SIMG_L2B_LST` | INSAT-3DS | Land surface temperature | half-hourly | the 06:00 UTC file each day | [10.19038/SAC/10/3SIMG_L2B_LST](https://doi.org/10.19038/SAC/10/3SIMG_L2B_LST) |
| `3RIMG_L3B_HEM_DLY` | INSAT-3DR | Daily rainfall (Hydro-Estimator) | daily | every file | [10.19038/SAC/10/3RIMG_L3B_HEM_DLY](https://doi.org/10.19038/SAC/10/3RIMG_L3B_HEM_DLY) |
| `3SIMG_L3B_HEM_DLY` | INSAT-3DS | Daily rainfall (Hydro-Estimator) | daily | every file | [10.19038/SAC/10/3SIMG_L3B_HEM_DLY](https://doi.org/10.19038/SAC/10/3SIMG_L3B_HEM_DLY) |

**Why these:** the same quantity from two satellites enables the cross-satellite check. INSAT-3D
(the original) returned no data for 2025–2026 searches, so we use the 3DR + 3DS pair. Daily LST
exists only for INSAT-3DS, so for LST we take one half-hourly file per day from each satellite at
nearly the same time (06:00 and 06:15 UTC ≈ 11:30–11:45 IST; the 15-minute offset is noted in the
cross-check). SST (`3RIMG_L3B_SST_DLY`, `3SIMG_L3B_SST_DLY`) is available for a later coastal
extension.

**Study window:** 2025-10-01 to 2025-11-30 (61 days). A `search` on 2026-10-08 found 60 + 60 LST
files and 54 + 53 rainfall files – about 2.0 GB in total, downloaded once and deleted after
conversion.

### Access and terms

- **Account:** searching is open; downloading needs a free MOSDAC account (General User).
- **Download API:** MOSDAC's official Data Download API is available
  (<https://mosdac.gov.in/downloadapi-manual>). TrustOrbit's `MosdacClient` calls the same
  endpoints as MOSDAC's `mdapi.py` but fetches only the one time slot per day we need.
- **Terms** ([MOSDAC Data Dissemination Guidelines](https://mosdac.gov.in/look/DOCS/mosdac-data-guidelines_english.pdf)):
  - Credit line (mandatory): **"Data Source MOSDAC/SAC/ISRO. https://mosdac.gov.in"**, plus the
    product DOI.
  - Downloaded products may **not** be resold or redistributed; value-added products may be
    distributed. We therefore never commit the raw HDF5 files. We commit only our value-added
    station extracts (one pixel per station per file, with our quality status) and our scores.
  - Data are provided "as is" on a best-effort basis.

### How the data in `data/isro/` was obtained

1. **Register (once).** Go to <https://mosdac.gov.in/signup/>. Fill in user name (min 5
   characters, lower case, first 3 letters alphabetic), a strong password, title, name, email,
   mobile, organisation (your college), designation (Student), city, country and purpose (e.g.
   "Academic project: reliability scoring of INSAT-3DR/3DS LST and rainfall"). Accept the
   agreement, solve the captcha and submit. Confirm the email link. New accounts are approved as
   General Users.
2. **Save the login locally.** In the repository root, copy `.env.example` to `.env` and fill in
   `MOSDAC_USERNAME` and `MOSDAC_PASSWORD`. `.env` is git-ignored.
3. **Check what will be fetched (no login):**
   `./mvnw -q exec:java -Dexec.args="search 2025-10-01 2025-11-30"` (in `java/`).
4. **Download and convert:**
   `./mvnw -q exec:java -Dexec.args="download 2025-10-01 2025-11-30"`.
   Each file is downloaded to `data/raw/`, converted to station rows in `data/isro/*.csv`, then
   deleted. Re-running skips files already converted (useful after MOSDAC's limit of 5,000 files
   per day or a network drop).
5. **Manual alternative:** download the same files from the MOSDAC website (Data Access → Order
   Data) or with `mdapi.py` and its `gId` option, put them in any folder, and run
   `./mvnw -q exec:java -Dexec.args="convert <folder>"`.

## 2. Bhoonidhi – NRSC, ISRO

<https://bhoonidhi.nrsc.gov.in> – ISRO's Earth-observation data hub (Resourcesat, Cartosat, EOS).
Requires registration; high-resolution imagery is outside this project's scope. **Future work.**

## 3. Bhuvan – NRSC, ISRO

<https://bhuvan.nrsc.gov.in> – a public OGC WMS endpoint answers without login
(`https://bhuvan-vec1.nrsc.gov.in/bhuvan/wms?service=WMS&request=GetCapabilities`, checked
2026-10-08). Planned as an optional website map overlay, credited "© NRSC/ISRO Bhuvan".

## Basemap (website)

CARTO Dark Matter tiles, © OpenStreetMap contributors © CARTO (added in Phase 4).

## Synthetic data

Synthetic data appears only in tests (e.g. the tiny NetCDF file built in `IsroFileReaderTest`) and
in the evaluation command and Reliability Lab scenarios (Phases 3–4). It is always generated in
code, labelled **SYNTHETIC**, and never mixed into `data/isro/`.
