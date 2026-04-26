# CI/CD en TrueNAS SCALE sin Dockge (paso a paso)

Guia actualizada para tu caso real:

- Ahora mismo no usas Dockge.
- El repo vive en tu PC (escritorio).
- Quieres actualizar facil, sin compilar en el NAS.

## Checklist rapido

- [ ] Subir el repo del escritorio a GitHub
- [ ] Configurar CI (test + build)
- [ ] Configurar CD (publicar imagen en GHCR con tags)
- [ ] Desplegar en TrueNAS usando la imagen publicada
- [ ] Actualizar cambiando solo el tag de imagen
- [ ] Hacer rollback al tag anterior si algo falla

---

## 1) Arquitectura simple (recomendada)

Flujo:

1. Trabajas en tu PC (repo local).
2. Haces push a GitHub.
3. GitHub Actions prueba y publica imagen en `ghcr.io`.
4. TrueNAS solo descarga esa imagen (no compila).
5. Para actualizar, cambias tag `vX.Y.Z` y redeploy.

Ventajas: menos carga en el NAS, updates rapidos, rollback inmediato.

---

## 2) Llevar el repo del escritorio a GitHub

Si aun no lo hiciste, desde tu PC:

```powershell
Set-Location "C:\Users\carlo\Desktop\raterr"
git init
git add .
git commit -m "init raterr"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/raterr.git
git push -u origin main
```

Si el repo ya existe, omite los pasos que no apliquen.

---

## 3) CI en GitHub Actions (push/PR)

Crea `/.github/workflows/ci.yml`:

```yaml
name: CI

on:
  pull_request:
    branches: ["main"]
  push:
    branches: ["main"]

jobs:
  test-and-build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - name: Maven test
        run: mvn -B test

      - name: Docker build smoke
        run: docker build -t raterr:ci .
```

---

## 4) CD en GitHub Actions (publicar imagen)

Crea `/.github/workflows/release.yml`:

```yaml
name: Release Image

on:
  push:
    tags:
      - 'v*.*.*'

jobs:
  publish:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup QEMU
        uses: docker/setup-qemu-action@v3

      - name: Setup Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login GHCR
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ghcr.io/${{ github.repository_owner }}/raterr
          tags: |
            type=ref,event=tag
            type=sha
            type=raw,value=latest

      - name: Build and push
        uses: docker/build-push-action@v6
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
```

---

## 5) Despliegue en TrueNAS sin Dockge

Como no tienes Dockge, usa la via nativa que tengas disponible en tu TrueNAS:

### Opcion A (normal): Apps / Custom App

Si tu panel muestra algo como "Apps" y permite imagen custom:

- Imagen: `ghcr.io/TU_USUARIO_O_ORG/raterr:v1.0.0`
- Puerto contenedor: `8080`
- Puerto host: `8080` (o el que prefieras)
- Volumen persistente: dataset -> `/data`
- Variables:
  - `PORT=8080`
  - `SQLITE_DB_PATH=/data/raterr.db`
  - `TMDB_API_KEY=TU_API_KEY`

### Opcion B: instalar Dockge mas adelante

Si luego quieres una UI mas comoda para stacks, puedes instalar Dockge y reutilizar `deploy/dockge/compose.yaml`.

---

## 6) Tu flujo diario para actualizar

### Publicar version nueva

```powershell
Set-Location "C:\Users\carlo\Desktop\raterr"
git checkout main
git pull
git tag v1.0.1
git push origin main --tags
```

Cuando termine `release.yml`, la imagen `v1.0.1` estara en GHCR.

### Actualizar en TrueNAS

- Edita la app/stack y cambia imagen a:
  - `ghcr.io/TU_USUARIO_O_ORG/raterr:v1.0.1`
- Redeploy.
- Verifica:
  - `http://IP_TRUENAS:8080/api/health`
  - `http://IP_TRUENAS:8080/index.html`

---

## 7) Rollback rapido

Si falla algo tras actualizar:

- Vuelve al tag anterior (ej. `v1.0.0`).
- Redeploy.

Ejemplo:

- `ghcr.io/TU_USUARIO_O_ORG/raterr:v1.0.0`

Tip: si tu dataset tiene snapshots, haz snapshot antes de actualizar.

---

## 8) Buenas practicas minimas

- Usa tags inmutables (`v1.0.0`, `v1.0.1`, ...).
- Deja `main` siempre desplegable.
- Secretos fuera de Git (`TMDB_API_KEY` solo en TrueNAS).
- Mantener `latest` como comodidad, pero desplegar por version fija.
