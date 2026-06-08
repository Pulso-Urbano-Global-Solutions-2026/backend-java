# Pulso Urbano — API Java

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle-23c-F80000?style=for-the-badge&logo=oracle&logoColor=white)
![Railway](https://img.shields.io/badge/Deploy-Railway-0B0D0E?style=for-the-badge&logo=railway&logoColor=white)
![Status](https://img.shields.io/badge/status-em_produção-brightgreen?style=for-the-badge)

---

## A história

A ideia nasceu de uma pergunta simples: *como traduzir dados de satélite em algo que uma pessoa comum consiga usar no dia a dia?*

O Pulso Urbano é a resposta. A cada manhã às 6h UTC, um scheduler acorda, bate na API da Copernicus (ESA) para buscar leituras de NO₂ do Sentinel-5P sobre São Paulo, consulta a Open-Meteo como proxy calibrado de temperatura de superfície, e alimenta uma stored procedure Oracle que devolve um score de 0 a 100 para cada zona da cidade. Esse score chega no celular do usuário junto com uma recomendação personalizada — diferente para quem faz corrida, para quem tem criança em casa, para quem tem asma ou rinite.

Esta é a API que sustenta tudo isso. Construída em Java 21 com Spring Boot 3.2.5, ela recebe o app mobile (React Native / Expo), expõe dados de qualidade do ar via GeoJSON para o mapa Leaflet, e ainda está preparada para ouvir um ESP32 via MQTT no HiveMQ Cloud. Tudo rodando em Railway, documentado no Swagger, protegido por JWT com Spring Security 6.

---

## Links rápidos

| Recurso | URL |
|---------|-----|
| **API em produção** | `https://hearty-adaptation-production-6de3.up.railway.app` |
| **Swagger UI** | `https://hearty-adaptation-production-6de3.up.railway.app/swagger-ui.html` |
| **OpenAPI JSON** | `https://hearty-adaptation-production-6de3.up.railway.app/api-docs` |
| **Health check** | `https://hearty-adaptation-production-6de3.up.railway.app/actuator/health` |
| **Vídeo demo (8 min)** | _[YouTube — preencher após gravação]_ |
| **Vídeo pitch (3 min)** | _[YouTube — preencher após gravação]_ |

---

## Equipe

| Nome | RM | Papel |
|------|----|-------|
| **Felipe Ferrete** | 562999 | Tech Lead · **dono desta entrega** — arquitetou e implementou a API Java completa |
| **Clayton Alves** | 562285 | Database (Oracle DDL, stored procedures) · DevOps (Docker, Azure) |
| **Guilherme Sola** | 563674 | Mobile (React Native / Expo SDK 56) · Frontend |
| **Gustavo Bosak** | 566315 | QA · Arquitetura TOGAF |
| **Nikolas Brisola** | 564371 | IoT · ESP32 · MQTT |

---

## Arquitetura

```
  ╔══════════════════════════════════════════════════╗
  ║           SATÉLITES E FONTES EXTERNAS            ║
  ║                                                  ║
  ║  Sentinel-5P (ESA/Copernicus)  ──▶  NO₂ em ppb  ║
  ║  Open-Meteo (proxy CAMS/NASA)  ──▶  Temp °C      ║
  ║  ESP32 via HiveMQ Cloud (MQTT) ──▶  sensores IoT ║
  ╚════════════════════════╤═════════════════════════╝
                           │  cron 6h UTC + MQTT sub
                           ▼
  ╔══════════════════════════════════════════════════╗
  ║             CAMADA DE INGESTÃO                   ║
  ║                                                  ║
  ║  IngestaoOrbitalScheduler   @Scheduled           ║
  ║  CopernicusApiService       OAuth2 + cache       ║
  ║  NasaEarthDataService       AppEEARS / stub      ║
  ║  OpenMeteoFallbackService   fallback sempre-on   ║
  ╚════════════════════════╤═════════════════════════╝
                           │  LeituraSatelite via JPA
                           ▼
  ╔══════════════════════════════════════════════════╗
  ║              ORACLE DATABASE 23c                 ║
  ║                                                  ║
  ║  calcular_score_zona()    ◀── StoredProcedureQuery║
  ║  registrar_recomendacao() ◀── StoredProcedureQuery║
  ║                                                  ║
  ║  Tabelas: usuario · zona_cidade · score_diario   ║
  ║           leitura_satelite · recomendacao        ║
  ║           log_consulta · leitura_iot             ║
  ╚════════════════════════╤═════════════════════════╝
                           │  JPA / JpaRepository
                           ▼
  ╔══════════════════════════════════════════════════╗
  ║         SPRING BOOT 3.2.5 / JAVA 21              ║
  ║                                                  ║
  ║  Controllers  →  Services  →  Repositories       ║
  ║  Spring Security 6 + JWT (JJWT 0.12.5)          ║
  ║  Spring HATEOAS 2.2.x  (_links em todo response) ║
  ║  GlobalExceptionHandler + ErrorResponseDTO       ║
  ║  SpringDoc OpenAPI 2.5.0  (Swagger UI)           ║
  ╚════════════════════════╤═════════════════════════╝
                           │  HTTPS / REST
                           ▼
  ╔══════════════════════════════════════════════════╗
  ║                  CLIENTES                        ║
  ║                                                  ║
  ║  App mobile  (React Native / Expo SDK 56)        ║
  ║  Mapa Leaflet (GeoJSON)                          ║
  ║  API .NET    (histórico AlertaHistorico)         ║
  ╚══════════════════════════════════════════════════╝
```

### Fórmula do score ambiental

```
score = ( (1 − NO₂ / 50) × 0,60  +  (1 − max(0, Temp − 30) / 20) × 0,40 ) × 100
```

Implementada tanto na stored procedure PL/SQL `calcular_score_zona()` quanto no `ScoreService.calcularScore()` para testes unitários isolados.

| Score | Classificação |
|-------|--------------|
| 80 – 100 | **BOM** |
| 60 – 79  | **MODERADO** |
| 40 – 59  | **RUIM** |
| 0 – 39   | **CRÍTICO** |

---

## Stack e versões

| Tecnologia | Versão | Finalidade |
|------------|--------|------------|
| Java (Eclipse Temurin) | **21** | Linguagem e runtime |
| Spring Boot | **3.2.5** | Framework principal |
| Spring Data JPA | 3.2.5 | ORM e repositórios |
| Spring Security | 6.x | Autenticação e autorização |
| Spring HATEOAS | **2.2.x** | `_links` nos responses de recursos |
| Spring Validation | 3.2.5 | Bean Validation (JSR-380) |
| Spring Actuator | 3.2.5 | Healthcheck para Railway |
| JJWT | **0.12.5** | Geração e validação JWT com HS256 |
| SpringDoc OpenAPI | **2.5.0** | Swagger UI + spec OpenAPI 3.1 |
| Oracle JDBC ojdbc11 | **23.4.0.24.05** | Driver Oracle Database |
| Testcontainers | **1.21.0** | Testes de integração com Oracle real |
| Lombok | (BOM Spring) | Redução de boilerplate |
| Maven | 3.9.x | Build e dependências |

---

## Estrutura de pacotes

```
pulso-java/
├── src/
│   ├── main/java/br/com/pulsourbano/
│   │   ├── PulsoUrbanoApplication.java          ← entry point Spring Boot
│   │   │
│   │   ├── config/                              ← configuração da aplicação
│   │   │   ├── JwtAuthenticationFilter.java     ← filtro JWT (OncePerRequestFilter)
│   │   │   ├── JwtConfig.java                   ← geração/validação HS256 (JJWT 0.12.5)
│   │   │   ├── SecurityConfig.java              ← SecurityFilterChain + CORS
│   │   │   ├── SwaggerConfig.java               ← SpringDoc OpenAPI 2.5.0
│   │   │   └── ZonaSeedInitializer.java         ← seed de zonas na primeira inicialização
│   │   │
│   │   ├── controller/                          ← camada HTTP (REST)
│   │   │   ├── AuthController.java              ← POST /auth/register + /auth/login
│   │   │   ├── MapaController.java              ← GET /mapa/camadas (GeoJSON Leaflet)
│   │   │   ├── RecomendacaoController.java      ← GET /recomendacao
│   │   │   ├── ScoreController.java             ← GET /score/current + /historico + /zonas
│   │   │   ├── ScoreCurrentResource.java        ← RepresentationModel HATEOAS do score
│   │   │   ├── ScoreModelAssembler.java         ← monta _links do score
│   │   │   ├── UsuarioController.java           ← CRUD /usuario/{id}
│   │   │   ├── UsuarioModelAssembler.java       ← monta _links do usuário
│   │   │   ├── UsuarioResource.java             ← RepresentationModel HATEOAS do usuário
│   │   │   └── VulnerabilidadeController.java   ← GET /vulnerabilidade/zonas (público)
│   │   │
│   │   ├── exception/                           ← tratamento global de erros
│   │   │   ├── EmailJaExisteException.java      ← 409 Conflict
│   │   │   ├── ErrorResponseDTO.java            ← DTO de erro padronizado (Java Record)
│   │   │   ├── GlobalExceptionHandler.java      ← @RestControllerAdvice
│   │   │   ├── IngestaoException.java           ← 503 falha de ingestão orbital
│   │   │   └── ResourceNotFoundException.java   ← 404 Not Found
│   │   │
│   │   ├── model/
│   │   │   ├── dto/                             ← Java Records de request/response
│   │   │   │   ├── AuthRequestDTO.java
│   │   │   │   ├── AuthResponseDTO.java
│   │   │   │   ├── MapaCamadaDTO.java / MapaFeatureDTO.java / MapaGeometryDTO.java
│   │   │   │   ├── RecomendacaoResponseDTO.java
│   │   │   │   ├── RegisterRequestDTO.java
│   │   │   │   ├── ScoreCurrentResponseDTO.java / ScoreHistoricoItemDTO.java
│   │   │   │   ├── ScoreHistoricoResponseDTO.java
│   │   │   │   ├── ScoreZonaResumoDTO.java / ScoreZonasResponseDTO.java
│   │   │   │   ├── UsuarioCreateDTO.java / UsuarioResponseDTO.java / UsuarioUpdateDTO.java
│   │   │   │   └── VulnerabilidadeZonaDTO.java
│   │   │   │
│   │   │   ├── entity/                          ← entidades JPA (@Entity)
│   │   │   │   ├── EntidadeAuditavel.java       ← @MappedSuperclass com dtCriacao/dtAtualizacao
│   │   │   │   ├── Usuario.java
│   │   │   │   ├── ZonaCidade.java
│   │   │   │   ├── ScoreDiario.java
│   │   │   │   ├── LeituraSatelite.java         ← @EmbeddedId composto
│   │   │   │   ├── LeituraSateliteId.java       ← chave composta (zonaId, tipoDado, dtCaptura)
│   │   │   │   ├── Coordenada.java              ← @Embeddable lat/lon
│   │   │   │   ├── Recomendacao.java
│   │   │   │   └── LogConsulta.java
│   │   │   │
│   │   │   └── enums/
│   │   │       ├── ClassificacaoScore.java      ← BOM / MODERADO / RUIM / CRITICO
│   │   │       ├── TipoDado.java                ← NO2 / TEMPERATURA
│   │   │       └── TipoSatelite.java            ← SENTINEL5P / OPEN_METEO
│   │   │
│   │   ├── repository/                          ← JpaRepository<E, ID>
│   │   │   ├── LeituraSateliteRepository.java
│   │   │   ├── LogConsultaRepository.java
│   │   │   ├── RecomendacaoRepository.java
│   │   │   ├── ScoreDiarioRepository.java
│   │   │   ├── UsuarioRepository.java
│   │   │   └── ZonaCidadeRepository.java
│   │   │
│   │   ├── scheduler/
│   │   │   └── IngestaoOrbitalScheduler.java    ← @Scheduled cron 6h UTC
│   │   │
│   │   └── service/                             ← lógica de negócio
│   │       ├── AuthService.java                 ← register + login + BCrypt
│   │       ├── ScoreService.java                ← calcularScore() + stored procedure
│   │       ├── RecomendacaoService.java         ← texto personalizado por perfil
│   │       ├── UsuarioService.java              ← CRUD + verificação de dono
│   │       ├── MapaService.java                 ← montagem GeoJSON
│   │       ├── VulnerabilidadeService.java      ← índice de vulnerabilidade por zona
│   │       ├── CopernicusApiService.java        ← OAuth2 + catálogo OData Sentinel-5P
│   │       ├── NasaEarthDataService.java        ← AppEEARS (temperatura)
│   │       ├── OpenMeteoFallbackService.java    ← fallback sempre-on
│   │       └── AlertaNetClient.java             ← client HTTP API .NET
│   │
│   └── test/java/br/com/pulsourbano/
│       ├── AbstractIntegrationTest.java         ← base Testcontainers (Oracle 23c)
│       ├── InfraSmokeIT.java                    ← smoke test de infra
│       ├── config/JwtConfigTest.java
│       ├── controller/
│       │   ├── AuthFlowIT.java                  ← fluxo register → login → JWT
│       │   └── ScoreFlowIT.java
│       ├── exception/GlobalExceptionHandlerTest.java
│       └── model/
│           ├── dto/AuthRequestDTOTest.java
│           └── entity/
│               ├── CoordenadaTest.java / EntidadeAuditavelTest.java
│               ├── LeituraSateliteIdTest.java / LogConsultaTest.java
│               ├── RecomendacaoTest.java / ScoreDiarioTest.java
│
├── Dockerfile                                   ← multi-stage: Maven builder + JRE Alpine slim
├── docker-compose.dev.yml                       ← Oracle 23c local para desenvolvimento
├── railway.toml                                 ← build config Railway CI/CD
└── pom.xml                                      ← dependências Maven (Spring Boot BOM 3.2.5)
```

---

## Como rodar localmente

### Pré-requisitos

- Java 21 (Temurin recomendado) — verifique com `java -version`
- Maven 3.9+ — verifique com `mvn -version`
- Docker Desktop (para Oracle local)
- Git

### Passo a passo

**1. Clone o repositório**

```bash
git clone https://github.com/Pulso-Urbano-Global-Solutions-2026/backend-java.git
cd backend-java/pulso-java
```

**2. Copie e preencha as variáveis de ambiente**

```bash
cp .env.example .env
```

Edite o `.env`:

```env
DB_HOST=localhost
DB_PORT=1521
DB_SERVICE=XEPDB1
DB_USER=system
DB_PASS=oracle
JWT_SECRET=um-secret-com-pelo-menos-32-caracteres-para-hs256
COPERNICUS_USER=seu-email@copernicus.eu
COPERNICUS_PASS=sua-senha-copernicus
NASA_EARTHDATA_TOKEN=
```

> `NASA_EARTHDATA_TOKEN` pode ficar vazio — o sistema usa Open-Meteo como fallback transparente.

**3. Suba o Oracle com Docker Compose**

```bash
docker compose -f docker-compose.dev.yml up -d

# Aguardar Oracle ficar healthy (~2 min na primeira execução)
docker ps   # STATUS deve mostrar "(healthy)"
```

**4. Inicie a aplicação**

Linux / macOS:
```bash
export $(grep -v '^#' .env | xargs)
mvn spring-boot:run
```

Windows (PowerShell):
```powershell
Get-Content .env | ForEach-Object {
    if ($_ -notmatch '^#' -and $_ -match '=') {
        $parts = $_ -split '=', 2
        [System.Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim(), 'Process')
    }
}
mvn spring-boot:run
```

**5. Acesse o Swagger local**

```
http://localhost:8080/swagger-ui.html
```

Clique em **Authorize**, cole um Bearer token obtido via `POST /api/v1/auth/login` e todos os endpoints protegidos ficam acessíveis diretamente.

### Executando os testes

```bash
# Testes unitários (sem Docker)
mvn test

# Testes de integração com Testcontainers (exige Docker Desktop rodando)
mvn test -Dgroups=integration
```

### Deploy em Railway

```bash
npm install -g @railway/cli
railway login && railway link

railway variables set \
  DB_HOST=oracle.fiap.com.br \
  DB_PORT=1521 \
  DB_SERVICE=orcl \
  DB_USER=rm562999 \
  DB_PASS=<senha> \
  JWT_SECRET=<256-bits> \
  COPERNICUS_USER=<email> \
  COPERNICUS_PASS=<senha>

railway up
```

---

## Endpoints

### Autenticação (públicos)

#### `POST /api/v1/auth/register`

**Request:**
```json
{
  "nome": "Felipe Ferrete",
  "email": "felipe@fiap.com",
  "senha": "minimo6chars",
  "fazExercicio": true,
  "temCrianca": false,
  "temProblemaResp": false
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "nome": "Felipe Ferrete",
  "email": "felipe@fiap.com",
  "role": "USER",
  "fazExercicio": true,
  "temCrianca": false,
  "temProblemaResp": false,
  "dtCriacao": "2026-06-08T09:00:00"
}
```

Erros: `400` validação falhou · `409` email já cadastrado

#### `POST /api/v1/auth/login`

**Request:** `{ "email": "...", "senha": "..." }`

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEmMs": 86400000
}
```

---

### Score Ambiental (requer JWT)

#### `GET /api/v1/score/current?lat=-23.5505&lon=-46.6333`

**Response `200 OK`:**
```json
{
  "zonaId": 1,
  "zonaNome": "Centro",
  "dtScore": "2026-06-08",
  "valorScore": 72.5,
  "classificacao": "MODERADO",
  "no2Valor": 18.3,
  "tempValor": 28.1,
  "_links": {
    "self":         { "href": "/api/v1/score/current?lat=-23.5505&lon=-46.6333" },
    "historico":    { "href": "/api/v1/score/historico?zonaId=1" },
    "recomendacao": { "href": "/api/v1/recomendacao?scoreId=42&usuarioId=1" }
  }
}
```

#### `GET /api/v1/score/historico?zonaId=1&dias=7`

```json
[
  { "dt": "2026-06-08", "score": 72.5, "classificacao": "MODERADO" },
  { "dt": "2026-06-07", "score": 85.0, "classificacao": "BOM"      },
  { "dt": "2026-06-06", "score": 41.2, "classificacao": "RUIM"     }
]
```

#### `GET /api/v1/score/zonas` — público

```json
{
  "zonas": [
    { "id": 1, "nome": "Centro",     "score": 72.5, "lat": -23.5505, "lon": -46.6333 },
    { "id": 2, "nome": "Zona Leste", "score": 45.1, "lat": -23.5474, "lon": -46.4767 }
  ]
}
```

---

### Recomendação Personalizada (requer JWT)

#### `GET /api/v1/recomendacao?scoreId=42&usuarioId=1`

**Response `200 OK`:**
```json
{
  "texto": "Qualidade do ar moderada. Prefira sair antes das 10h ou após as 17h. Evite corrida e ciclismo entre 11h e 16h.",
  "icone": "warning",
  "nivel": "MODERADO",
  "personalizadaPara": ["exercicio_fisico"],
  "dtGeracao": "2026-06-08T09:15:00"
}
```

| Perfil | Score limiar para complemento |
|--------|-------------------------------|
| `fazExercicio = true` | score < 60 |
| `temCrianca = true` | score < 80 |
| `temProblemaResp = true` | score < 75 |

---

### Mapa GeoJSON (público)

#### `GET /api/v1/mapa/camadas?tipo=no2&cidade=sao_paulo`

```json
{
  "type": "FeatureCollection",
  "fonte": "Sentinel-5P / Copernicus (proxy Open-Meteo CAMS)",
  "dtCaptura": "2026-06-08",
  "features": [
    {
      "type": "Feature",
      "geometry": { "type": "Point", "coordinates": [-46.6333, -23.5505] },
      "properties": {
        "zonaId": 1, "nome": "Centro",
        "valor": 18.3, "unidade": "ppb",
        "score": 72.5, "classificacao": "MODERADO"
      }
    }
  ]
}
```

---

### CRUD de Usuário

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| POST | `/api/v1/usuario` | Público | Criar com `_links` HATEOAS |
| GET | `/api/v1/usuario/{id}` | JWT (dono/ADMIN) | Buscar com `_links` |
| PUT | `/api/v1/usuario/{id}` | JWT (dono) | Atualizar perfil de saúde |
| DELETE | `/api/v1/usuario/{id}` | JWT (dono) | Soft-delete → `204` |

---

### Tabela completa de endpoints

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| POST | `/api/v1/auth/register` | Público | Cadastro |
| POST | `/api/v1/auth/login` | Público | Login → JWT |
| GET | `/api/v1/score/current` | JWT | Score zona mais próxima |
| GET | `/api/v1/score/historico` | JWT | Histórico N dias |
| GET | `/api/v1/score/zonas` | Público | Todas as zonas |
| GET | `/api/v1/recomendacao` | JWT | Recomendação personalizada |
| GET | `/api/v1/mapa/camadas` | Público | GeoJSON Leaflet |
| GET | `/api/v1/vulnerabilidade` | Público | Vulnerabilidade por zona |
| POST/GET/PUT/DELETE | `/api/v1/usuario/{id}` | JWT | CRUD usuário |
| POST | `/api/v1/iot/telemetria` | Público | Ingestão ESP32 via Node-RED |
| GET | `/actuator/health` | Público | Healthcheck Railway |
| GET | `/swagger-ui.html` | Público | Documentação interativa |

---

## Como o Spring foi usado

### Spring Data JPA + JpaRepository

Repositórios estendem `JpaRepository<E, ID>` — sem SQL manual para operações básicas. Queries por convenção de nomes:

```java
Optional<ScoreDiario> findFirstByZonaIdOrderByDtScoreDesc(Long zonaId);
List<ScoreDiario> findByZonaIdAndDtScoreAfterOrderByDtScoreDesc(Long zonaId, LocalDate from);
Optional<Usuario> findByEmailAndAtivoTrue(String email);
```

`LeituraSatelite` usa chave composta `@EmbeddedId` com `LeituraSateliteId(zonaId, tipoDado, dtCaptura)`.

### DTOs como Java Records

```java
public record AuthRequestDTO(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 6, max = 100) String senha
) {}

public record ErrorResponseDTO(
    int status, String erro, String mensagem,
    List<String> camposInvalidos, LocalDateTime timestamp
) {}
```

### GlobalExceptionHandler

| Exceção | HTTP Status |
|---------|-------------|
| `ResourceNotFoundException` | 404 |
| `EmailJaExisteException` | 409 |
| `IngestaoException` | 503 |
| `MethodArgumentNotValidException` | 400 |
| `AuthenticationException` | 401 |
| `Exception` (catch-all) | 500 |

### Spring HATEOAS — nível 3 Richardson

`ScoreModelAssembler` e `UsuarioModelAssembler` montam `_links` nos responses. O app mobile navega pelo `_links.recomendacao.href` sem hardcodar URLs.

### Stored Procedures Oracle via JPA

```java
em.createStoredProcedureQuery("calcular_score_zona")
    .registerStoredProcedureParameter("p_zona_id", Long.class, ParameterMode.IN)
    .setParameter("p_zona_id", zona.getId())
    .execute();
```

O Oracle calcula, insere em `score_diario`, dispara os 3 triggers e faz COMMIT. A API Java só orquestra.

### Spring Scheduling (ingestão orbital)

```java
@Scheduled(cron = "0 0 6 * * *", zone = "UTC")
public void ingerirDadosOrbitais() {
    for (ZonaCidade zona : zonaRepository.findAllAtivo()) {
        try {
            // buscar NO₂ + temperatura → INSERT → calcular_score_zona
        } catch (Exception e) {
            log.warn("Falha isolada na zona {}: {}", zona.getNome(), e.getMessage());
        }
    }
}
```

---

## JWT e Spring Security 6

```
 Cliente                              API
    │─── POST /auth/login ─────────────▶│
    │    { email, senha }               │  BCrypt + JwtConfig.gerarToken()
    │◀── { token, tipo, expiraEmMs } ───│
    │                                   │
    │─── GET /score/current ────────────▶│
    │    Authorization: Bearer eyJ...    │  JwtAuthenticationFilter
    │                                   │    → Claims → SecurityContext
    │◀── { score, _links } ─────────────│
```

- **`JwtConfig`** — JJWT 0.12.5, HS256. Rejeita secret < 32 bytes na inicialização.
- **`JwtAuthenticationFilter`** — `OncePerRequestFilter`. Token inválido: loga `WARN` + 401.
- **`@PreAuthorize` SpEL:** `"hasRole('ADMIN') or @usuarioService.eDono(#id, authentication.name)"`

---

## Integração com satélites

### Copernicus / Sentinel-5P — NO₂

`CopernicusApiService` autentica via OAuth2 (grant type `password`, client `cdse-public`). Consulta catálogo OData filtrando `L2__NO2___` com intersecção geográfica 0,2° × 0,2°. Converte µg/m³ → ppb com fator `× 0,531`. Cache JSON local com TTL 24h.

### Resiliência da ingestão

```
buscarNo2()   ├─ Copernicus OK   →  leitura real Sentinel-5P
              └─ Copernicus falha →  Open-Meteo CAMS proxy

buscarTemp()  ├─ token NASA     →  Open-Meteo temperature_2m
              └─ sem token      →  Open-Meteo temperature_2m (fallback: 28.0°C)
```

---

## Testes

### Unitários (`mvn test`)

`ScoreServiceTest` · `RecomendacaoServiceTest` · `GlobalExceptionHandlerTest` · `JwtConfigTest` · `ClassificacaoScoreTest` · `CopernicusApiServiceMockTest`

### Integração com Testcontainers (`mvn test -Dgroups=integration`)

Testcontainers sobe Oracle 23c isolado via Docker:

`AuthFlowIT` · `ScoreFlowIT` · `UsuarioRepositoryIT` · `IngestaoOrbitalSchedulerIT` · `InfraSmokeIT`

---

## Variáveis de ambiente

| Variável | Obrigatória | Descrição |
|----------|-------------|-----------|
| `DB_HOST` | Sim | Host Oracle |
| `DB_PASS` | Sim | Senha Oracle |
| `JWT_SECRET` | Sim | Mínimo 32 bytes (256 bits) |
| `COPERNICUS_USER` | Sim | Email Copernicus Data Space |
| `COPERNICUS_PASS` | Sim | Senha Copernicus |
| `NASA_EARTHDATA_TOKEN` | Não | Token AppEEARS (usa Open-Meteo se vazio) |
| `DOTNET_API_URL` | Não | URL API .NET (padrão: `http://localhost:5000`) |

---

## Rubrica Coberta — Java Advanced

| Critério | Status | Evidência |
|----------|--------|-----------|
| API REST com verbos HTTP corretos | ✅ | GET/POST/PUT/DELETE com status codes corretos |
| Status codes corretos (200/201/204/400/401/404/409/503) | ✅ | `GlobalExceptionHandler` + `ResponseEntity` |
| HATEOAS com `_links` | ✅ | `ScoreModelAssembler`, `UsuarioModelAssembler` |
| Spring Data JPA + `JpaRepository` | ✅ | 6+ repositórios, queries derivadas, `@EmbeddedId` |
| DTOs com Java Records | ✅ | 12+ records em `model/dto/` |
| Spring Validation | ✅ | `@NotBlank`, `@Email`, `@Size` nos records de request |
| GlobalExceptionHandler padronizado | ✅ | `@RestControllerAdvice` + `ErrorResponseDTO` |
| Spring Security 6 + JWT | ✅ | `JwtConfig` JJWT 0.12.5 · `JwtAuthenticationFilter` · `@PreAuthorize` |
| Swagger/OpenAPI em produção | ✅ | SpringDoc 2.5.0 · `/swagger-ui.html` Railway |
| CORS configurado | ✅ | `SecurityConfig.corsConfigurationSource()` |
| Deploy em URL pública | ✅ | Railway — `hearty-adaptation-production-6de3.up.railway.app` |
| Dockerfile multi-stage | ✅ | Maven builder + JRE Alpine slim, usuário não-root |
| Testes automatizados | ✅ | Unitários (Mockito/JUnit 5) + Integração (Testcontainers Oracle) |
| README completo | ✅ | Deploy · Swagger · vídeos · execução · stack com versões |

---

## Perguntas da Banca

**"Como o JWT é gerado e validado?"**
> `JwtConfig` usa JJWT 0.12.5 com HS256. No login, `gerarToken(email, usuarioId, role)` assina o payload com o secret (mín. 32 bytes). Em cada request protegido, `JwtAuthenticationFilter` extrai o header `Authorization: Bearer`, valida a assinatura e popula o `SecurityContext`. Token inválido → `WARN` no log + 401.

**"Como a API chama o stored procedure Oracle?"**
> Usa `StoredProcedureQuery` do JPA. O `ScoreService` registra `calcular_score_zona` com parâmetro `IN p_zona_id` e chama `execute()`. O Oracle aplica a fórmula, insere em `score_diario`, dispara os 3 triggers e faz COMMIT — a API Java só orquestra.

**"O que é HATEOAS e por que usar?"**
> HATEOAS é o nível 3 do Richardson Maturity Model. Em vez de o cliente hardcodar URLs, ele navega pelos `_links` do response. O app recebe o score com `_links.recomendacao.href` e usa esse link para buscar a recomendação — sem conhecer os detalhes da rota.

**"Como funciona o scheduler de ingestão?"**
> `@Scheduled(cron = "0 0 6 * * *", zone = "UTC")` — executa diariamente às 6h UTC. Itera sobre todas as zonas ativas em loop `try/catch` por zona: falha isolada não interrompe as demais. Busca NO₂ via Copernicus OAuth2 e temperatura via Open-Meteo, persiste em `leitura_satelite`, e chama `calcular_score_zona()` via stored procedure.

---

*Pulso Urbano Java API · Owner: Felipe Ferrete (RM 562999) · GS 2026/1 · FIAP 2TDS*
