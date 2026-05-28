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

Swagger UI disponível em: `http://localhost:8080/swagger-ui.html`

---

## Deploy

A API é containerizada via `pulso-java/Dockerfile` e orquestrada pelo `docker-compose.yml` na raiz do monorepo. Para deploy em cloud, configure as variáveis de ambiente no provedor e aponte para uma instância Oracle acessível.
