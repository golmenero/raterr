# Raterr

App web para buscar peliculas en TMDB, valorarlas por categorias y generar tops.

## Stack
- Backend: Kotlin + Ktor
- DB: SQLite (archivo local)
- Frontend: HTML/CSS/JS puro

## Funcionalidades
- Busqueda de peliculas en TMDB
- Valoracion por categorias:
  - Direccion
  - Fotografia
  - Actuacion
  - Banda sonora
  - Guion
- Calculo de media por pelicula
- Maximo una valoracion por pelicula (si te equivocas, se elimina desde Tops)
- Tops:
  - General
  - Por anio

## Modelo de datos
- Tabla `movie`: metadatos de TMDB
- Tabla `rating`: valoraciones por categoria

## Variables de entorno
- `TMDB_API_KEY` (obligatoria para consultar TMDB)
- `PORT` (opcional, default `8080`)
- `SQLITE_DB_PATH` (opcional, default `raterr.db`)

## Ejecutar
```powershell
$env:TMDB_API_KEY="TU_API_KEY"
mvn clean compile exec:java
```

Abrir en navegador:
- `http://localhost:8080/index.html`
- `http://localhost:8080/top.html`

## Docker

### Build de imagen
```powershell
docker build -t raterr .
```

### Ejecutar contenedor
```powershell
docker run --name raterr -p 8080:8080 -e TMDB_API_KEY="TU_API_KEY" -e PORT="8080" -e SQLITE_DB_PATH="/data/raterr.db" -v raterr_data:/data raterr
```

### Docker Compose
```powershell
# opcion A: variable en la sesion
$env:TMDB_API_KEY="TU_API_KEY"
docker compose up --build
```

Tambien puedes usar un archivo `.env` en la raiz del proyecto (puedes partir de `.env.example`):

```env
TMDB_API_KEY=TU_API_KEY
PORT=8080
SQLITE_DB_PATH=/data/raterr.db
```

Compose inyecta estas variables en el contenedor en `docker-compose.yml`.

La base SQLite queda persistida en el volumen Docker `raterr_data`.

## Endpoints
- `GET /api/health`
- `GET /api/search?q=...`
- `GET /api/search/suggestions?q=...&limit=5`
- `GET /api/movie/{id}`
- `POST /api/rate`
- `DELETE /api/movie/{id}/rating`
- `GET /api/tops?year=2024` (sin `limit` devuelve todos)

Ejemplo payload para `POST /api/ratings`:
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

## Dockge en TrueNAS SCALE (ligero)
Archivos listos para usar:
- `deploy/dockge/compose.yaml` (recomendado: imagen publicada)
- `deploy/dockge/compose.build.yaml` (alternativa: build en NAS)
- `deploy/dockge/.env.example` (variables del stack)
- `docs/DEPLOY_DOCKGE.md` (pasos rapidos)

Flujo minimo:
1. Copia `deploy/dockge/.env.example` a `.env` en tu stack Dockge.
2. Ajusta `TMDB_API_KEY` y `RATERR_DATA_DIR` (ruta `/mnt/<pool>/...`).
3. Despliega con uno de los `compose` anteriores.
4. Verifica en `http://IP_TRUENAS:8080/api/health`.

### Publicar imagen en GHCR (tags)
Al crear un tag `v*`, el workflow `release.yml` publica en GHCR:
- `ghcr.io/<owner>/raterr:vX.Y.Z`
- `ghcr.io/<owner>/raterr:latest`

Comandos de ejemplo:
```powershell
git tag v1.0.0
git push origin v1.0.0
```

En TrueNAS/Dockge usa `RATERR_IMAGE=ghcr.io/<owner>/raterr:vX.Y.Z`.
