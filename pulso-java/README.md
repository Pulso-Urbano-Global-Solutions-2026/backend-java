# Pulso Urbano — Java API

## Visão

API primária do Pulso Urbano. Transforma dados orbitais reais (Sentinel-5P / ECOSTRESS) em score de qualidade ambiental e recomendações personalizadas para habitantes de São Paulo.

**Global Solution 2026/1 · FIAP · ADS 2º ano**

---

## Stack

| Tecnologia | Versão |
|---|---|
| Java | 21 (Temurin) |
| Spring Boot | 3.2.5 |
| Spring Security + JWT | 6.x + JJWT 0.12.5 |
| Spring Data JPA | 3.2.x |
| Oracle JDBC | ojdbc11 23.4.0.24.05 |
| SpringDoc OpenAPI | 2.5.0 |
| Spring HATEOAS | 2.2.x |

---

## Como rodar

### Pré-requisitos

- Java 21
- Maven 3.9+
- Oracle rodando em `localhost:1521/XEPDB1` (via Docker)

### Variáveis de ambiente obrigatórias

```bash
export DB_USER=system
export DB_PASS=oracle
export JWT_SECRET=pulso-urbano-secret-key-2026-gs-fiap
export COPERNICUS_USER=seu@email.com
export COPERNICUS_PASS=suaSenha
export NASA_EARTHDATA_TOKEN=seuToken
```

### Compilar e rodar

```bash
cd pulso-java
mvn clean package -DskipTests
java -jar target/pulso-urbano-*.jar
```

### Via Docker Compose (recomendado)

```bash
# na raiz do monorepo
docker compose up --build
```

---

## Endpoints

| Método | Rota | Auth | Descrição |
|---|---|---|---|
| POST | `/api/v1/auth/login` | — | Login, retorna JWT |
| POST | `/api/v1/auth/register` | — | Cadastro de usuário |
| GET | `/api/v1/score/current` | JWT | Score atual por coordenada |
| GET | `/api/v1/score/historico` | JWT | Histórico de scores |
| GET | `/api/v1/score/zonas` | — | Scores de todas as zonas |
| GET | `/api/v1/recomendacao` | JWT | Recomendação personalizada |
| GET | `/api/v1/mapa/camadas` | — | Camadas GeoJSON (NO₂/temp) |
| POST | `/api/v1/usuario` | — | Criar usuário |
| GET | `/api/v1/usuario/{id}` | JWT | Buscar usuário |
| PUT | `/api/v1/usuario/{id}` | JWT | Atualizar usuário |
| DELETE | `/api/v1/usuario/{id}` | JWT | Deletar usuário |

Swagger UI disponível em:
- Local: `http://localhost:8080/swagger-ui.html`
- Produção: `https://hearty-adaptation-production-6de3.up.railway.app/swagger-ui.html`

---

## Deploy

A API é containerizada via `pulso-java/Dockerfile` e orquestrada pelo `docker-compose.yml` na raiz do monorepo.

### Variáveis de ambiente

Copie `.env.example` para `.env` e preencha os valores reais:

```bash
cp .env.example .env
# editar .env com credenciais reais
```

### Dev local (Oracle via Docker)

```bash
# Subir Oracle local (primeira vez leva ~3 min)
docker compose -f docker-compose.dev.yml up -d

# Rodar a API
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
mvn spring-boot:run
```

> **Testcontainers:** para rodar testes de integração (`*IT.java`), habilite primeiro:
> Docker Desktop → Settings → General → **"Expose daemon on tcp://localhost:2375 without TLS"**

### Deploy Fly.io (recomendado para Oracle)

Fly.io suporta sidecar containers Oracle XE, o que viabiliza Oracle real em produção gratuita.

```bash
# 1. Instalar CLI e autenticar
curl -L https://fly.io/install.sh | sh
flyctl auth login

# 2. Inicializar app (executar na pasta pulso-java)
flyctl launch --dockerfile Dockerfile --name pulso-urbano-562999

# 3. Configurar variáveis de ambiente
flyctl secrets set \
  DB_HOST=oracle-sidecar \
  DB_USER=system \
  DB_PASS=oracle \
  JWT_SECRET=<secret-256-bits> \
  COPERNICUS_USER=<email> \
  COPERNICUS_PASS=<senha>

# 4. Deploy
flyctl deploy
```

### Deploy Railway (produção atual)

A API está deployada no Railway conectada ao Oracle FIAP (`oracle.fiap.com.br:1521/orcl`).

```bash
# Redeploy (Railway CLI já configurado)
cd pulso-java
railway up --detach --service "hearty-adaptation"
```

Variáveis configuradas no Railway:
- `DB_HOST=oracle.fiap.com.br`, `DB_PORT=1521`, `DB_SERVICE=orcl`, `DB_USER=rm562999`
- `SPRING_JPA_HIBERNATE_DDL_AUTO=none`
- `SPRING_MAIN_LAZY_INITIALIZATION=true`
- `SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=1`
- `JWT_SECRET`, `COPERNICUS_USER`/`COPERNICUS_PASS`, `NASA_EARTHDATA_TOKEN` configurados

### URLs públicas

| Recurso | URL |
|---|---|
| API base | `https://hearty-adaptation-production-6de3.up.railway.app` |
| Swagger UI | `https://hearty-adaptation-production-6de3.up.railway.app/swagger-ui.html` |
| Health | `https://hearty-adaptation-production-6de3.up.railway.app/actuator/health` |
| Vídeo demo (8min) | _[YouTube link — preencher após gravação]_ |
| Vídeo pitch (3min) | _[YouTube link — preencher após gravação]_ |
