# Raterr

Web app to search for movies on TMDB, rate them by categories, and generate top lists.

## Stack
- Backend: Kotlin + Ktor
- DB: SQLite (local file)
- Frontend: Pure HTML/CSS/JS

## Features
- Movie search on TMDB
- Rating by categories:
  - Direction
  - Photography
  - Acting
  - Soundtrack
  - Script
- Average score calculation per movie
- Maximum one rating per movie (if you make a mistake, it's deleted from Tops)
- Tops:
  - General
  - By year

## Data Model
- `movie` table: TMDB metadata
- `rating` table: ratings per category

## Environment Variables
- `TMDB_API_KEY` (required to query TMDB)
- `PORT` (optional, default `8080`)
- `SQLITE_DB_PATH` (optional, default `raterr.db`)

## Run
```powershell
$env:TMDB_API_KEY="YOUR_API_KEY"
mvn clean compile exec:java
```

Open in browser:
- `http://localhost:8080/index.html`
- `http://localhost:8080/top.html`

## Docker

### Build Image
```powershell
docker build -t raterr .
```

### Run Container
```powershell
docker run --name raterr -p 8080:8080 -e TMDB_API_KEY="YOUR_API_KEY" -e PORT="8080" -e SQLITE_DB_PATH="/data/raterr.db" -v raterr_data:/data raterr
```

### Docker Compose
```powershell
# Option A: variable in session
$env:TMDB_API_KEY="YOUR_API_KEY"
docker compose up --build
```

You can also use a `.env` file in the project root (you can start from `.env.example`):

```env
TMDB_API_KEY=YOUR_API_KEY
PORT=8080
SQLITE_DB_PATH=/data/raterr.db
```

Compose injects these variables into the container in `docker-compose.yml`.

The SQLite database is persisted in the Docker volume `raterr_data`.

## Endpoints
- `GET /api/health`
- `GET /api/search?q=...`
- `GET /api/search/suggestions?q=...&limit=5`
- `GET /api/movie/{id}`
- `POST /api/rate`
- `DELETE /api/movie/{id}/rating`
- `GET /api/tops?year=2024` (without `limit` returns all)

Example payload for `POST /api/ratings`:
```json
{
  "tmdbId": 603,
  "direccion": 9,
  "fotografia": 8,
  "actuacion": 9,
  "bandaSonora": 8,
  "guion": 9
}
```

## Dockge on TrueNAS SCALE (lightweight)
Ready-to-use files:
- `deploy/dockge/compose.yaml` (recommended: published image)
- `deploy/dockge/compose.build.yaml` (alternative: build on NAS)
- `deploy/dockge/.env.example` (stack variables)
- `docs/DEPLOY_DOCKGE.md` (quick steps)

Minimal flow:
1. Copy `deploy/dockge/.env.example` to `.env` in your Dockge stack.
2. Adjust `TMDB_API_KEY` and `RATERR_DATA_DIR` (path `/mnt/<pool>/...`).
3. Deploy with one of the above `compose` files.
4. Verify at `http://IP_TRUENAS:8080/api/health`.

### Publishing Image to GHCR (tags)
When you create a tag `v*`, the `release.yml` workflow publishes to GHCR:
- `ghcr.io/<owner>/raterr:vX.Y.Z`
- `ghcr.io/<owner>/raterr:latest`

Example commands:
```powershell
git tag v1.0.0
git push origin v1.0.0
```

On TrueNAS/Dockge use `RATERR_IMAGE=ghcr.io/<owner>/raterr:vX.Y.Z`.
