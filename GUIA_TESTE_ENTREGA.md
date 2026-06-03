# Guia Completo — Testar, Validar e Entregar
**Pulso Urbano · Global Solution 2026/1 · FIAP · Deadline: 09/06/2026**

---

## 0. Problema mais comum — Java errado no PATH

Seu terminal padrão aponta para Java 17. O projeto **exige Java 21**.  
Sempre que abrir um terminal novo para este projeto, rode primeiro:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH

# Confirmar:
java -version
# → openjdk version "21.x.x"
```

Se o `mvn test` retornar erro `class file version 65.0`, isso é o Java errado.

---

## 1. Pré-requisitos

```powershell
java -version     # precisa: 21 (ver seção 0 se mostrar 17)
mvn -version      # precisa: 3.9+
docker --version  # qualquer versão com CLI funcionando
```

---

## 2. Variáveis de ambiente

### O que cada variável é e onde buscar

| Variável | Valor dev local | Onde encontrar/criar |
|---|---|---|
| `DB_USER` | `system` | Fixo — usuário padrão do Oracle XE |
| `DB_PASS` | `oracle` | Fixo — senha do `docker-compose.dev.yml` |
| `DB_HOST` | `localhost` | Padrão; só muda no Docker Compose (onde vira `oracle`) |
| `DB_PORT` | `1521` | Padrão Oracle |
| `DB_SERVICE` | `XEPDB1` | Padrão Oracle XE (ou `FREEPDB1` no `gvenzl/oracle-free:23`) |
| `JWT_SECRET` | `pulso-urbano-secret-key-2026-gs-fiap-256bits-ok!` | **Invente qualquer string com 32+ chars** — só precisa ser longa |
| `COPERNICUS_USER` | `felipeferretelemes@gmail.com` | Sua conta em dataspace.copernicus.eu |
| `COPERNICUS_PASS` | sua senha | Senha da conta Copernicus |
| `NASA_EARTHDATA_TOKEN` | `""` (vazio) | Pode ficar vazio — fallback Open-Meteo entra automaticamente |

### Setar no terminal (rode uma vez por sessão)

```powershell
# Java correto primeiro
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH

# Banco (Oracle local via Docker)
$env:DB_USER    = "system"
$env:DB_PASS    = "oracle"
$env:DB_HOST    = "localhost"
$env:DB_PORT    = "1521"
$env:DB_SERVICE = "XEPDB1"

# JWT — qualquer string longa serve para dev
$env:JWT_SECRET = "pulso-urbano-secret-key-2026-gs-fiap-256bits-ok!"

# Copernicus (dados NO2 reais) — use suas credenciais
$env:COPERNICUS_USER = "felipeferretelemes@gmail.com"
$env:COPERNICUS_PASS = "SuaSenha"

# NASA — pode ficar vazio, Open-Meteo entra como fallback
$env:NASA_EARTHDATA_TOKEN = ""
```

---

## 3. Verificar credenciais Copernicus (opcional)

Se quiser confirmar que seu usuário/senha Copernicus funciona antes de subir a API:

```powershell
$body = "grant_type=password&client_id=cdse-public&username=felipeferretelemes@gmail.com&password=SuaSenha"
Invoke-RestMethod -Method POST `
  -Uri "https://identity.dataspace.copernicus.eu/auth/realms/CDSE/protocol/openid-connect/token" `
  -ContentType "application/x-www-form-urlencoded" `
  -Body $body | Select-Object access_token, expires_in
```

- Retornou `access_token` → credenciais OK, a API buscará NO₂ real do Sentinel-5P
- Retornou erro 401 → senha errada; a API continuará funcionando via Open-Meteo automaticamente

---

## 4. Subir Oracle local

```powershell
cd "D:\FIAP\GS2026-1\PU_backend-java\pulso-java"
docker compose -f docker-compose.dev.yml up -d
```

Primeira vez: aguardar ~3 minutos. Verificar se está pronto:

```powershell
docker logs pulso-oracle-dev-562999 --tail 5
# Aguardar aparecer: "DATABASE IS READY TO USE!"
```

O Hibernate cria o schema automaticamente na primeira execução  
(`spring.jpa.hibernate.ddl-auto=update` no profile dev).

---

## 5. Testes unitários

```powershell
cd "D:\FIAP\GS2026-1\PU_backend-java\pulso-java"

# Garantir Java 21 (se abriu terminal novo)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH

# Rodar todos os unit tests (excluindo IT)
mvn test "-Dtest=!*IT"
```

Resultado esperado:
```
Tests run: 44, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Classes de teste unitário (19 arquivos, ~44 testes):
- `ScoreServiceTest`, `ScoreServiceProximityTest`
- `RecomendacaoServiceTest`
- `CopernicusApiServiceTest`, `CopernicusApiServiceMockTest`
- `ClassificacaoScoreTest`
- `UsuarioTest`, `ZonaCidadeTest`, `ScoreDiarioTest`, `RecomendacaoTest`, `LogConsultaTest`
- `CoordenadaTest`, `EntidadeAuditavelTest`, `LeituraSateliteIdTest`
- `AuthRequestDTOTest`
- `JwtConfigTest`
- `GlobalExceptionHandlerTest`

---

## 6. Testes de integração (requer Docker)

Os ITs sobem um Oracle XE real via Testcontainers.

### Problema com Docker Desktop 4.34+

Docker Desktop mudou o protocolo de autenticação dos named pipes no Windows.  
O `docker-java` (usado pelo Testcontainers) não suporta esse novo protocolo.

### Solução A — Rancher Desktop (recomendada, gratuita)

1. Baixar em **rancherdesktop.io** → instalar
2. Durante setup: escolher **dockerd (moby)** como engine (não containerd)
3. Preferences → Container Engine → habilitar **Allow Privileged Mounts**
4. **Fechar Docker Desktop** (exit no system tray)
5. Aguardar Rancher Desktop inicializar

```powershell
# Confirmar que está funcionando
docker version

# Rodar ITs
cd "D:\FIAP\GS2026-1\PU_backend-java\pulso-java"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
mvn test "-Dtest=*IT"
```

Resultado esperado (primeiro run leva ~3-5 min para baixar a imagem Oracle):
```
Tests run: X, Failures: 0, Errors: 0
BUILD SUCCESS
```

### Solução B — Downgrade Docker Desktop

Baixar Docker Desktop 4.27.x (antes da mudança de auth).  
Disponível em: docs.docker.com/desktop/release-notes → buscar 4.27.

### Solução C — Rodar em CI (mais simples)

Em qualquer Linux/CI (GitHub Actions, etc.) os ITs funcionam sem ajuste:
```yaml
- run: mvn test -Dtest="*IT"
```

---

## 7. Build do JAR

```powershell
cd "D:\FIAP\GS2026-1\PU_backend-java\pulso-java"
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH

mvn clean package -DskipTests
# → target/pulso-urbano-0.0.1-SNAPSHOT.jar
```

---

## 8. Rodar a API localmente

```powershell
# (Oracle deve estar rodando — seção 4)
# (Variáveis de ambiente setadas — seção 2)
cd "D:\FIAP\GS2026-1\PU_backend-java\pulso-java"
mvn spring-boot:run
```

Aguardar:
```
Started PulsoUrbanoApplication in X.XXX seconds
```

---

## 9. Testar via Swagger UI

Abrir no browser: **http://localhost:8080/swagger-ui.html**

### Fluxo completo de teste

**a) Registrar usuário**
```
POST /api/v1/auth/register
{
  "nome": "Felipe Teste",
  "email": "felipe@teste.com",
  "senha": "senha123",
  "fazExercicio": true,
  "temCrianca": false,
  "temProblemaResp": false
}
→ 201 Created
```

**b) Login — copiar o token**
```
POST /api/v1/auth/login
{ "email": "felipe@teste.com", "senha": "senha123" }
→ 200 OK  { "token": "eyJ...", "tipo": "Bearer", "expiraEm": 86400000 }
```

**c) Autenticar no Swagger**  
Botão **Authorize** → colar apenas o token (sem a palavra "Bearer")

**d) Score atual (requer token)**
```
GET /api/v1/score/current?lat=-23.5505&lon=-46.6333
→ 200 OK  { "score": 62.4, "classificacao": "MODERADO", ..., "_links": {...} }
```

**e) Endpoints públicos (sem token)**
```
GET /api/v1/score/zonas           → lista todas as zonas com scores
GET /api/v1/mapa/camadas?tipo=no2&cidade=sao_paulo  → GeoJSON
GET /actuator/health              → {"status":"UP"}
```

---

## 10. Stack completa via Docker Compose

```powershell
# Criar o .env a partir do exemplo
cd "D:\FIAP\GS2026-1\PU_backend-java"
Copy-Item pulso-java\.env.example .env

# Editar .env com as credenciais reais:
# JWT_SECRET=pulso-urbano-secret-key-2026-gs-fiap-256bits-ok!
# COPERNICUS_USER=felipeferretelemes@gmail.com
# COPERNICUS_PASS=SuaSenha

# Subir tudo (Oracle + Java API)
docker compose up --build
```

A API sobe em http://localhost:8080 e o Oracle em localhost:1521.

---

## 11. T-42 — Deploy no Fly.io

### Passo 1 — Criar conta e instalar CLI

1. Criar conta grátis em **fly.io** (não precisa cartão para apps básicos)
2. Instalar CLI:

```powershell
# Via PowerShell
iwr https://fly.io/install.ps1 -useb | iex
```

3. Autenticar:
```powershell
flyctl auth login
# Abre o browser — fazer login com a conta criada
```

### Passo 2 — Inicializar o app

```powershell
cd "D:\FIAP\GS2026-1\PU_backend-java\pulso-java"
flyctl launch --dockerfile Dockerfile --name pulso-urbano-562999 --region gru
# Quando perguntar "Would you like to set up a Postgresql database?" → No
# Quando perguntar "Would you like to set up an Upstash Redis database?" → No
# Isso gera o fly.toml automaticamente
```

### Passo 3 — Banco de dados

**Opção A — Oracle Cloud Always Free (recomendada)**

1. Criar conta em **cloud.oracle.com** (Always Free, sem cartão)
2. Criar **Autonomous Database** (Always Free: 1 OCPU, 20 GB)
3. Após criar: Actions → **Download Client Credentials (Wallet)**
4. Copiar a connection string JDBC do tipo `pulso_high` (ou `pulso_tp`)

```powershell
flyctl secrets set `
  DB_HOST="adb.sa-saopaulo-1.oraclecloud.com" `
  DB_USER="ADMIN" `
  DB_PASS="SuaSenhaOracleCloud" `
  DB_SERVICE="pulso_high" `
  DB_PORT="1522" `
  JWT_SECRET="pulso-urbano-secret-key-2026-gs-fiap-256bits-ok!" `
  COPERNICUS_USER="felipeferretelemes@gmail.com" `
  COPERNICUS_PASS="SuaSenha" `
  NASA_EARTHDATA_TOKEN=""
```

**Opção B — Oracle como sidecar no Fly.io (simples mas usa RAM)**

Editar o `fly.toml` gerado e adicionar:

```toml
[[services]]
  internal_port = 8080
  protocol = "tcp"

  [[services.ports]]
    port = 443
    handlers = ["tls", "http"]

  [[services.ports]]
    port = 80
    handlers = ["http"]

# Sidecar Oracle (apenas para prova de conceito — usa ~1GB RAM)
[[processes]]
  oracle = "docker run -d -p 1521:1521 -e ORACLE_PASSWORD=oracle gvenzl/oracle-free:23-slim-faststart"
```

> Para prova de conceito, Opção B é mais rápida mas instável. Para entrega real, Opção A.

### Passo 4 — Ajustar fly.toml

Verificar que o `fly.toml` tem a porta correta:

```toml
[http_service]
  internal_port = 8080
  force_https = true
  auto_stop_machines = true
  auto_start_machines = true
```

### Passo 5 — Deploy

```powershell
cd "D:\FIAP\GS2026-1\PU_backend-java\pulso-java"
flyctl deploy
# Acompanhar logs:
flyctl logs
```

### Passo 6 — Smoke test em produção

```powershell
$BASE = "https://pulso-urbano-562999.fly.dev"

# Health
Invoke-RestMethod "$BASE/actuator/health"
# → { "status": "UP" }

# Registrar usuário
$body = '{"nome":"smoke","email":"smoke@gs.com","senha":"smoke123","fazExercicio":false,"temCrianca":false,"temProblemaResp":false}'
Invoke-RestMethod -Method POST "$BASE/api/v1/auth/register" `
  -ContentType "application/json" -Body $body
# → 201 Created

# Login e capturar token
$resp = Invoke-RestMethod -Method POST "$BASE/api/v1/auth/login" `
  -ContentType "application/json" `
  -Body '{"email":"smoke@gs.com","senha":"smoke123"}'
$token = $resp.token

# Score com token
Invoke-RestMethod "$BASE/api/v1/score/current?lat=-23.5505&lon=-46.6333" `
  -Headers @{ Authorization = "Bearer $token" }
# → { "score": ..., "classificacao": ..., "_links": {...} }

# Swagger UI
Start-Process "$BASE/swagger-ui.html"
```

### Passo 7 — Atualizar README com URL pública

Editar `pulso-java/README.md` e preencher a tabela de URLs:

```markdown
| API base     | `https://pulso-urbano-562999.fly.dev`              |
| Swagger UI   | `https://pulso-urbano-562999.fly.dev/swagger-ui.html` |
| Vídeo demo   | [YouTube link aqui]                                 |
| Vídeo pitch  | [YouTube link aqui]                                 |
```

Commitar:
```powershell
git add pulso-java/README.md
git commit -m "docs: update README with production URL and video links (task T-42)"
git tag v1.0.0-gs2026
git push && git push --tags
```

---

## 12. Checklist de entrega (09/06 até 23h55)

### Local
- [ ] `java -version` → mostra Java 21 (não 17)
- [ ] `mvn test "-Dtest=!*IT"` → **44 testes, 0 falhas**
- [ ] `mvn clean package -DskipTests` → JAR gerado sem erro
- [ ] `docker compose up` → Oracle + Java API sobem sem erro
- [ ] `http://localhost:8080/swagger-ui.html` → Swagger UI carrega
- [ ] `POST /api/v1/auth/register` → **201 Created**
- [ ] `POST /api/v1/auth/login` → **200 OK** com token JWT
- [ ] `GET /api/v1/score/current?lat=-23.55&lon=-46.63` (com token) → score + `_links` HATEOAS
- [ ] `GET /api/v1/score/zonas` (sem token) → lista pública
- [ ] `GET /api/v1/mapa/camadas?tipo=no2` (sem token) → GeoJSON

### Produção (T-42)
- [ ] `https://pulso-urbano-562999.fly.dev/actuator/health` → `{"status":"UP"}`
- [ ] Smoke test de registro + login + score na URL pública
- [ ] `README.md` atualizado com URL pública e links de vídeo
- [ ] `git tag v1.0.0-gs2026` criada e pushed
- [ ] Push para o repositório remoto que o professor vai clonar

---

## Resumo do estado do projeto

- **41/42 tasks implementadas** — código completo
- **T-42** = única task restante = só execução: criar conta Fly.io, deploy, atualizar README
- **Testes unitários (44):** passam com Java 21 + `mvn test "-Dtest=!*IT"`
- **Testes de integração (5 arquivos IT):** precisam de Rancher Desktop ou Linux/CI
- **Problema atual:** seu terminal usa Java 17 — fix na seção 0 e seção 2
