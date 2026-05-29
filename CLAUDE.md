# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## Project Identity

**Pulso Urbano** — Java API backend for a FIAP Global Solution 2026/1 project (ADS 2nd year).  
Transforms real orbital data (Sentinel-5P NO₂ / ECOSTRESS temperature) into an environmental quality score and personalized recommendations for São Paulo residents.

**Deadline:** 09/06/2026 · **Tech lead (this repo):** Felipe Ferrete (RM 562999)

---

## Commands

All commands run from `pulso-java/` directory.

```bash
# Build (skip tests)
mvn clean package -DskipTests

# Run locally (requires env vars — see below)
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ScoreServiceTest

# Run a single test method
mvn test -Dtest=ScoreServiceTest#calcularScore_no2Zero_retorna100

# Run integration tests only (*IT.java)
mvn test -Dtest="*IT"

# Run unit tests only (exclude IT)
mvn test -Dtest="!*IT"
```

**Integration tests** require Docker Desktop running with the Linux engine pipe exposed. Testcontainers spins up `gvenzl/oracle-free:23-slim-faststart` automatically.

On Windows, `pom.xml` already sets:
```
DOCKER_HOST=npipe:////./pipe/dockerDesktopLinuxEngine
```

### Required environment variables

```bash
DB_USER=system
DB_PASS=oracle
JWT_SECRET=<min-256-bits>
COPERNICUS_USER=<email>
COPERNICUS_PASS=<password>
NASA_EARTHDATA_TOKEN=<token>
```

`DB_HOST`, `DB_PORT`, `DB_SERVICE` default to `localhost`, `1521`, `XEPDB1`.

### Dev with local Oracle

```bash
# Start Oracle (first time ~3 min)
docker compose -f pulso-java/docker-compose.dev.yml up -d

# Run API
mvn spring-boot:run
```

### Full stack via Docker Compose

```bash
# From repo root (requires .env from .env.example)
docker compose up --build
```

---

## Architecture

### System overview

```
Sentinel-5P (ESA) + ECOSTRESS (NASA) + Open-Meteo (fallback)
        │ OAuth2 / REST
        ▼
Java API (Spring Boot 3.2, port 8080)  ←── this repo
        │ JPA
        ▼
Oracle XE (container, port 1521)
        │
.NET API (ASP.NET Core 8, port 5000) — manages alerts only
```

The Java API **owns** orbital ingestion, score calculation, recommendations, maps, auth, and the USUARIO/ZONA_CIDADE/LEITURA_SATELITE/SCORE_DIARIO/RECOMENDACAO/LOG_CONSULTA tables.  
The .NET API owns ALERTA_HISTORICO — **never duplicate domains across the two APIs**.

### Package structure (`br.com.pulsourbano`)

```
config/          SecurityConfig, JwtConfig, SwaggerConfig
controller/      ScoreController, RecomendacaoController, MapaController,
                 UsuarioController, AuthController + HATEOAS assemblers
service/         ScoreService, RecomendacaoService, UsuarioService, AuthService,
                 CopernicusApiService, NasaEarthDataService, OpenMeteoFallbackService,
                 MapaService
repository/      JpaRepository interfaces for each entity
model/entity/    JPA entities (Usuario, ZonaCidade, LeituraSatelite,
                 ScoreDiario, Recomendacao, LogConsulta)
model/dto/       Java Records only — never expose entities in responses
model/enums/     ClassificacaoScore (BOM/MODERADO/RUIM/CRITICO),
                 TipoDado (NO2/TEMP_SUPERFICIE/UV),
                 TipoSatelite (SENTINEL_5P/ECOSTRESS/OMI)
exception/       GlobalExceptionHandler (@RestControllerAdvice),
                 ResourceNotFoundException, IngestaoException, ErrorResponseDTO
scheduler/       IngestaoOrbitalScheduler (@Scheduled daily at 06:00 UTC)
```

### Score algorithm (source of truth — same logic in Oracle PL/SQL procedure)

```java
// 60% air quality (NO₂) + 40% temperature
double scorNo2   = Math.max(0, 1 - (no2Ppb / 50.0));
double scoreTemp = Math.max(0, 1 - Math.max(0, (tempC - 30.0) / 20.0));
double score     = Math.round((scorNo2 * 0.60 + scoreTemp * 0.40) * 100 * 10.0) / 10.0;

// Classification
score >= 80 → BOM | >= 60 → MODERADO | >= 40 → RUIM | else → CRITICO
```

### Data ingestion flow

`IngestaoOrbitalScheduler` → `CopernicusApiService.buscarNo2()` + `NasaEarthDataService.buscarTempSuperficie()` → `ScoreService.calcularEPersistir()` — runs for every `ZonaCidade` row. Exceptions are caught per zone (log + continue, never abort the loop). `OpenMeteoFallbackService` is used when Copernicus is unavailable.

### Recommendation engine

Template-based switch in `RecomendacaoService.gerarTexto()` — no LLM. Personalization appended based on user flags: `fazExercicio`, `temCrianca`, `temProblemaRespiratorio`.

### Security

JWT (JJWT 0.12.5) via `JwtAuthenticationFilter`. Public endpoints: `/api/v1/auth/**`, `/api/v1/score/zonas`, `/api/v1/mapa/**`, `/swagger-ui.html`, `/api-docs`, `/actuator/health`.

### Test strategy

- **Unit tests** (`*Test.java`): pure JUnit 5 + Mockito, no Spring context.
- **Integration tests** (`*IT.java`): extend `AbstractIntegrationTest`, which boots a single shared `OracleContainer` (Testcontainers). The container is static — started once per test suite run, not once per class.
- `application-test.properties` sets `ddl-auto=create-drop` so Hibernate creates the schema fresh on each IT run.

---

## Absolute rules

**Never do:**
- Use LLM/AI to generate recommendation text — templates only
- Set `ddl-auto=create-drop` or `create` in `application.properties` (production profile)
- Run containers as root — always `USER pulso` (uid 1001)
- Hardcode credentials — all secrets via env vars
- Expose JPA entities directly in REST responses — always use DTOs (Records)
- Duplicate Java domain in .NET (e.g., score calculation, auth)
- Process raw NetCDF files — use Copernicus Level-3 pre-processed products

**Always do:**
- Add `_links` (HATEOAS) to every Java endpoint response
- Validate request body with `@Valid` + Bean Validation annotations
- Document every endpoint in Swagger (`@Operation`, `@ApiResponse`)
- Catch exceptions from orbital APIs per-zone (log + continue)
- Name containers with RM 562999 suffix (DevOps grading rule)
