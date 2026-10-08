# Contributing to TrustOrbit

Welcome aboard, crew! This guide gets you from zero to your first pull request.

## 1. One-time setup

You need **Git**, **Docker Desktop**, **JDK 21**, **Maven 3.9+**, **Node 22+** and **VS Code**.
Easiest option: open the repo in VS Code and choose **"Reopen in Container"** (or open it in
GitHub Codespaces) – the `.devcontainer` installs everything for you.

```bash
git clone https://github.com/Rajiv-6969/trustorbit.git
cd trustorbit
git config core.hooksPath .githooks     # enables the pre-commit format check
cp .env.example .env                    # optional: add a FIRMS key later
```

Install the recommended VS Code extensions when prompted (`.vscode/extensions.json`).

## 2. Run everything

| What | Command |
|---|---|
| Database + API | `docker compose up --build` |
| Only the database | `docker compose up -d db` |
| API from source (debuggable) | `mvn -f backend/pom.xml spring-boot:run` or F5 → "Debug TrustOrbit backend" |
| Front end | `cd frontend && npm install && npm run dev` |
| Back-end unit tests | `mvn -f backend/pom.xml test` |
| All back-end tests (needs Docker) | `mvn -f backend/pom.xml verify` |
| Front-end tests | `cd frontend && npm test` |
| Sample data summary | `python scripts/fetch-samples/summarize_samples.py` |

## 3. Where your code lives

| Who | Module | Folder |
|---|---|---|
| Akash | Setup, integration, CI | `backend/.../common/`, `.github/`, `docker-compose.yml` |
| Abhi Rathod | M1 Ingestion | `backend/src/main/java/com/trustorbit/ingestion/` |
| Sameer Basha | M2 Preprocessing | `backend/src/main/java/com/trustorbit/preprocessing/` |
| Rajiv Siddharth | M3 Quality engine | `backend/src/main/java/com/trustorbit/quality/` |
| Saikirantejas GS | M4 Dashboard & reports | `backend/src/main/java/com/trustorbit/dashboard/`, `frontend/` |

Tests mirror the same packages under `backend/src/test/java/`. Each module folder has a
`README.md` explaining what it does and why.

## 4. Branch → commit → pull request

`main` is protected: nobody pushes to it directly.

```bash
git switch main && git pull
git switch -c feat/m2-imputation        # feat/<module>-<topic>, fix/<module>-<topic>
# ...code...
mvn -f backend/pom.xml spotless:apply   # auto-format Java
git add -A
git commit -m "feat(m2): linear interpolation for short gaps"
git push -u origin feat/m2-imputation
```

Then open a pull request on GitHub. Fill in the template, link the issue (`Closes #7`), and
ask a teammate for review. CI must be green and one approval is required to merge. UI changes
get an automatic Vercel preview link on the PR.

### Commit messages (Conventional Commits)

`type(scope): short summary` – types: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `ci`.
Scopes: `m1`, `m2`, `m3`, `m4`, `ui`, `infra`, `data`, `docs`.

## 5. Code style

- Small classes, meaningful names, Javadoc on every public method, no dead code.
- Java is formatted with **google-java-format** via Spotless; TypeScript with **Prettier** +
  **ESLint**. VS Code formats on save; the pre-commit hook and CI check it.
- Unit tests use the **real sample data** in `data/samples/`.

## 6. Data & secrets rules

- Never commit `.env`, API keys or passwords.
- Never commit raw satellite files (HDF5/NetCDF/GeoTIFF) or anything > 5 MB – convert to small
  CSVs under `data/isro/` instead.
- Every new data file needs an entry in `DATA_SOURCES.md`.
