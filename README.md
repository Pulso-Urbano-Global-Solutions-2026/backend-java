# Pulso Urbano — Backend Java

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=flat-square&logo=spring-boot&logoColor=white)
![Oracle](https://img.shields.io/badge/Oracle-23c-F80000?style=flat-square&logo=oracle&logoColor=white)
![Railway](https://img.shields.io/badge/Deploy-Railway-0B0D0E?style=flat-square&logo=railway&logoColor=white)
![Status](https://img.shields.io/badge/status-em_produção-brightgreen?style=flat-square)

**FIAP Global Solution 2026/1 · ADS 2º ano · Tema: Economia Espacial**

Transforma dados orbitais reais (Sentinel-5P NO₂ + ECOSTRESS temperatura) em um score ambiental de 0–100 e recomendações personalizadas para moradores de São Paulo.

---

## Links rápidos

| Recurso | URL |
|---------|-----|
| **API em produção** | `https://hearty-adaptation-production-6de3.up.railway.app` |
| **Swagger UI** | `https://hearty-adaptation-production-6de3.up.railway.app/swagger-ui.html` |
| **OpenAPI JSON** | `https://hearty-adaptation-production-6de3.up.railway.app/api-docs` |
| **Health check** | `https://hearty-adaptation-production-6de3.up.railway.app/actuator/health` |
| **README completo da API** | [`pulso-java/README.md`](pulso-java/README.md) |
| **Vídeo pitch (3 min)** | https://youtu.be/dGy20MDjnL4 |
| **Vídeo demo (8 min)** | https://youtu.be/ZTe-8x3eBoc |

---

## O problema

Todo dia, milhões de pessoas em São Paulo tomam decisões sem informação ambiental adequada:

> *"Vou correr agora?"* — sem saber que o NO₂ está 3× acima do limite da OMS.  
> *"Levo meu filho a pé?"* — sem saber que a temperatura de superfície está em 48°C.

O dado já existe. O Sentinel-5P da ESA mede NO₂ sobre São Paulo diariamente, de graça. O ECOSTRESS da NASA mede temperatura real do asfalto, de graça. **O que faltava era o sistema que transforma esse dado orbital em decisão humana.**

---

## Arquitetura do sistema

```
┌─────────────────────────────────────────────────────────┐
│                    FONTES ORBITAIS                       │
│  Sentinel-5P (ESA/Copernicus) ──── NO₂ em ppb           │
│  Open-Meteo (proxy CAMS/NASA) ──── Temperatura °C        │
│  ESP32 via HiveMQ Cloud (MQTT) ─── sensores IoT          │
└──────────────────────┬──────────────────────────────────┘
                       │  cron 6h UTC + MQTT sub
                       ▼
         ┌─────────────────────────┐
         │   JAVA API  · :8080     │  ◀── este repositório
         │   Spring Boot 3.2.5     │
         │   Score · Auth · Mapa   │
         │   Recomendação · HATEOAS│
         └────────────┬────────────┘
                      │ JPA
                      ▼
         ┌─────────────────────────┐
         │   ORACLE DATABASE 23c   │
         │   Procedures · Triggers │
         └────────────┬────────────┘
                      │
          ┌───────────┴───────────┐
          ▼                       ▼
  ┌───────────────┐     ┌──────────────────┐
  │  App Mobile   │     │  API .NET · :5000 │
  │  React Native │     │  Alertas histórico│
  │  Expo SDK 56  │     │  ASP.NET Core 8   │
  └───────────────┘     └──────────────────┘
```

### Score ambiental

```
score = ( (1 − NO₂ / 50) × 0,60  +  (1 − max(0, Temp − 30) / 20) × 0,40 ) × 100
```

| Score | Classificação |
|-------|--------------|
| 80–100 | **BOM** |
| 60–79 | **MODERADO** |
| 40–59 | **RUIM** |
| 0–39 | **CRÍTICO** |

---

## Endpoints principais

| Método | Rota | Auth | Descrição |
|--------|------|------|-----------|
| `POST` | `/api/v1/auth/register` | Público | Cadastro |
| `POST` | `/api/v1/auth/login` | Público | Login → JWT |
| `GET` | `/api/v1/score/current?lat=&lon=` | JWT | Score da zona mais próxima |
| `GET` | `/api/v1/score/zonas` | Público | Score de todas as zonas |
| `GET` | `/api/v1/recomendacao` | JWT | Recomendação personalizada |
| `GET` | `/api/v1/mapa/camadas` | Público | GeoJSON para Leaflet |
| `GET` | `/api/v1/vulnerabilidade/zonas` | Público | Índice de vulnerabilidade |
| `CRUD` | `/api/v1/usuario/{id}` | JWT | Gerenciar perfil |
| `GET` | `/actuator/health` | Público | Healthcheck Railway |

Documentação interativa completa: [Swagger UI](https://hearty-adaptation-production-6de3.up.railway.app/swagger-ui.html)

---

## Como rodar localmente

### Pré-requisitos
- Java 21 · Maven 3.9+ · Docker Desktop

### Inicialização rápida

```bash
# 1. Clone e entre na pasta do módulo Java
git clone <repo>
cd pulso-java

# 2. Copie as variáveis de ambiente
cp .env.example .env   # edite com suas credenciais

# 3. Suba o Oracle local
docker compose -f docker-compose.dev.yml up -d

# 4. Inicie a API
mvn spring-boot:run

# 5. Acesse o Swagger
# http://localhost:8080/swagger-ui.html
```

### Full stack com Docker Compose

```bash
# Na raiz do repositório
cp .env.example .env   # edite com suas credenciais
docker compose up --build
# Java API em :8080 · Oracle em :1521
```

### Testes

```bash
# Unitários (sem Docker)
mvn test -Dtest="!*IT"

# Integração com Testcontainers Oracle (exige Docker Desktop)
mvn test -Dgroups=integration
```

### Variáveis de ambiente obrigatórias

| Variável | Descrição |
|----------|-----------|
| `DB_USER` / `DB_PASS` | Credenciais Oracle |
| `JWT_SECRET` | Mínimo 32 caracteres |
| `COPERNICUS_USER` / `COPERNICUS_PASS` | Conta Copernicus Data Space |
| `NASA_EARTHDATA_TOKEN` | Opcional — usa Open-Meteo como fallback |

---

## Equipe

| Nome | RM | Papel |
|------|----|-------|
| **Felipe Ferrete** | 562999 | Tech Lead · API Java (este repositório) |
| **Clayton Alves** | 562285 | Database (Oracle DDL, procedures) · DevOps |
| **Guilherme Sola** | 563674 | Mobile (React Native / Expo) |
| **Gustavo Bosak** | 566315 | QA · Arquitetura TOGAF |
| **Nikolas Brisola** | 564371 | IoT · ESP32 · MQTT |

---

## Stack

| Tecnologia | Versão |
|------------|--------|
| Java (Eclipse Temurin) | 21 |
| Spring Boot | 3.2.5 |
| Spring Security + JJWT | 6.x + 0.12.5 |
| Spring HATEOAS | 2.2.x |
| SpringDoc OpenAPI | 2.5.0 |
| Oracle JDBC ojdbc11 | 23.4.0 |
| Testcontainers | 1.21.0 |

---

## Documentação detalhada

O README completo da API Java, com todos os contratos de request/response, exemplos de uso, testes, estrutura de pacotes e respostas para perguntas da banca está em:

**[`pulso-java/README.md`](pulso-java/README.md)**

---

*Pulso Urbano · FIAP GS 2026/1 · Owner desta entrega: Felipe Ferrete (RM 562999)*
