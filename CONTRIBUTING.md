# Contributing to TrustOrbit

This guide takes you from a fresh laptop to your first pull request.

## 1. One-time setup

Install **Git**, **JDK 21** (e.g. Eclipse Temurin), **Node 22 LTS** and **VS Code**. You do *not*
need to install Maven – use the wrapper (`./mvnw` or `.\mvnw.cmd`). Alternatively open the repo in
GitHub Codespaces; the `.devcontainer` sets up JDK + Node for you.

```bash
git clone https://github.com/Rajiv-6969/trustorbit.git
cd trustorbit
```

When VS Code asks, install the recommended extensions (Java pack, ESLint, Prettier).

## 2. Run things

| What | macOS / Linux / Git Bash | Windows PowerShell |
|---|---|---|
| Pipeline | `cd java && ./mvnw -q exec:java -Dexec.args="run"` | `cd java; .\mvnw.cmd -q exec:java "-Dexec.args=run"` |
| Java tests | `./mvnw verify` | `.\mvnw.cmd verify` |
| Website | `cd website && npm install && npm run dev` | same |
| Website tests | `npm test` | same |

In VS Code: **Run and Debug → "TrustOrbit: run pipeline"** (breakpoints work), or
**Terminal → Run Task → "Run pipeline" / "Start website"**.

## 3. Where your code lives

| Who | Module | Folder (under `java/src/main/java/com/trustorbit/`) |
|---|---|---|
| Akash | `Main`, shared model, CI | `Main.java`, `AppConfig.java`, `model/`, `.github/` |
| Abhi Rathod | M1 Ingestion | `ingestion/` |
| Sameer Basha | M2 Preprocessing | `preprocessing/` |
| Rajiv Siddharth | M3 Quality engine | `quality/` |
| Saikirantejas GS | M4 Output + website | `output/`, `website/` |

Tests mirror the same packages under `java/src/test/java/`. Each module folder has a `README.md`.
Tunable numbers (weights, thresholds) live in `java/src/main/resources/config.properties`.

## 4. Branch → commit → pull request

`main` is protected: changes arrive through pull requests with one approving review and green CI.

```bash
git switch main && git pull
git switch -c feat/m2-imputation          # feat/<module>-<topic> or fix/<module>-<topic>
# ...edit, run tests...
git add -A
git commit -m "feat(m2): linear interpolation for short gaps"
git push -u origin feat/m2-imputation
```

Open the pull request on GitHub, fill in the template, link the issue (`Closes #7`) and request a
review. Website changes get a Vercel preview link on the PR once Vercel is connected.

**Commit messages** follow Conventional Commits: `type(scope): summary` with types `feat`, `fix`,
`docs`, `test`, `refactor`, `chore`, `ci` and scopes `m1`–`m4`, `web`, `data`, `docs`.

## 5. Code style

- Plain Java: small classes, clear names, Javadoc on every public method, no unused code.
- VS Code formats on save (Google Java style for Java, Prettier for TypeScript).
- Unit tests use the committed data in `data/`; synthetic test data is always labelled SYNTHETIC.

## 6. Data and secrets

- Your MOSDAC login goes only in `.env` (copy `.env.example`). Never commit `.env`.
- Never commit raw satellite files (`.h5`, `.nc`) – MOSDAC does not allow redistributing them and
  they are large. Only our converted station CSVs in `data/isro/` are committed.
- Every new data file needs an entry in `DATA_SOURCES.md`.
