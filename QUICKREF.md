# PULSO URBANO — QUICK REF
# Versão compacta do CONTEXT.md para sessões rápidas

## PROJETO
App que transforma dados orbitais (Sentinel-5P NO₂ + ECOSTRESS temp) em score de saúde ambiental
para cidadãos de São Paulo. Score 0–100 → recomendação textual em linguagem humana.

## APIS ORBITAIS (todas gratuitas)
- Copernicus: https://identity.dataspace.copernicus.eu (OAuth2) → Sentinel-5P NO₂ 3.5km/dia
- NASA: https://appeears.earthdatacloud.nasa.gov (token) → ECOSTRESS temp 70m
- Fallback: https://api.open-meteo.com/v1/forecast (sem auth) → weather

## SCORE ALGORITHM (implementar IGUAL nos dois lados)
score = (max(0, 1 - no2_ppb/50) * 0.60 + max(0, 1-(max(0,temp_c-30)/20)) * 0.40) * 100
BOM≥80 | MODERADO≥60 | RUIM≥40 | CRITICO<40

## JAVA — porta 8080 — DOMÍNIO PRINCIPAL
Spring Boot 3.2 + JPA + Spring Security JWT + HATEOAS + Swagger
Endpoints: /api/v1/auth/** | /score/** | /recomendacao/** | /mapa/** | /usuario/**
Scheduler: @Scheduled cron "0 0 6 * * *" → ingestão orbital diária

## .NET — porta 5000 — DOMÍNIO: ALERTAS
ASP.NET Core 8 + EF Core + Oracle + Migrations
Endpoints: /api/alertas/** | /api/estatisticas/**
Entidades: ZonaReferencia (1) → AlertaHistorico (N)

## ORACLE — porta 1521 — 6 TABELAS JAVA + 2 TABELAS .NET
Java: USUARIO · ZONA_CIDADE · LEITURA_SATELITE · SCORE_DIARIO · RECOMENDACAO · LOG_CONSULTA
.NET: ZONA_REFERENCIA_NET · ALERTA_HISTORICO (criadas via EF Migration)

## REGRAS NÃO NEGOCIÁVEIS
❌ Sem LLM para recomendação (template if/elif)
❌ Sem rota saudável por rua (resolução insuficiente)
❌ Sem localhost no deploy final
❌ Sem root no container
✅ Variáveis de ambiente para todas as credentials
✅ Fallback Open-Meteo se Copernicus falhar
✅ Container names com RM 562999

## ENTREGAS — 09/06 · 23h55
Java: GitHub URL pública · Deploy público · Swagger · Vídeo 8min + Pitch 3min
.NET: GitHub · Migration aplicada · Vídeo pitch 3min
DevOps: docker-compose em nuvem · SELECT no banco · Diagrama draw.io

## RESPONSABILIDADES
Felipe  → Java API + .NET + Copernicus/NASA integration + JWT + Deploy
Clayton → Oracle DDL + PL/SQL + Docker + DevOps cloud
Guilherme → React Native mobile (consome Java API)
Bosak   → QA + TOGAF/ArchiMate + validação telas + docs
Brisola → ESP32 Wokwi + DHT22 + MQ135 + MQTT → Java backend
