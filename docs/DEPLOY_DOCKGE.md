# Deploy ligero en Dockge (TrueNAS SCALE)

## 1) Preparar datos persistentes
Crea una carpeta/dataset para SQLite:

- Ejemplo: `/mnt/tank/apps/raterr/data`

Asegura permisos de escritura para Docker.

## 2) Elegir modo de despliegue

### Opcion A (recomendada): imagen publicada
Usa `deploy/dockge/compose.yaml` y define `RATERR_IMAGE` en `.env`.

### Opcion B: build en el NAS
Usa `deploy/dockge/compose.build.yaml`.

## 3) Crear Stack en Dockge
1. New Stack
2. Pega el contenido del compose elegido
3. Crea variables desde `deploy/dockge/.env.example`
4. Deploy

## 4) Verificar
- `http://IP_TRUENAS:8080/api/health`
- `http://IP_TRUENAS:8080/index.html`

## 5) Problemas comunes
- Puerto ocupado: cambia `HOST_PORT`.
- Error de TMDB: revisa `TMDB_API_KEY`.
- SQLite no escribe: revisa permisos en `RATERR_DATA_DIR`.

