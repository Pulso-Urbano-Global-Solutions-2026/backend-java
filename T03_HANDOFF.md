# T-03 — Handoff: onde parou e o que falta

## O que foi feito

Todos os arquivos foram criados corretamente:

- `pulso-java/src/test/java/br/com/pulsourbano/AbstractIntegrationTest.java` — classe base com OracleContainer static
- `pulso-java/src/test/java/br/com/pulsourbano/InfraSmokeTest.java` — teste de smoke
- `pulso-java/src/test/resources/oracle-init.sql` — DDL mínimo de seed (USUARIO + ZONA_CIDADE)
- `pulso-java/src/test/resources/testcontainers.properties` — tenta fixar estratégia (sem efeito, explicado abaixo)
- `pom.xml` atualizado: Testcontainers 1.21.0 (era 1.19.7) + Surefire com env var DOCKER_HOST

O código está correto. O problema é 100% de configuração de Docker Desktop.

---

## Por que o teste não passa

**Causa raiz:** Docker Desktop 4.34+ introduziu autenticação obrigatória para conexões via named pipe (soquetes nomeados do Windows). O Docker CLI autentica automaticamente usando o credential store `desktop`. O Testcontainers (qualquer versão) não implementa esse fluxo de autenticação — ele faz requisições HTTP puras sem credenciais.

**O que acontece quando roda `mvn test -Dtest=InfraSmokeTest`:**

1. Testcontainers tenta conectar em `\\.\pipe\docker_engine`
2. Docker Desktop responde `HTTP 400` com body vazio + label `com.docker.desktop.address=npipe://\\.\pipe\docker_cli`
3. Testcontainers não entende o redirect → falha com `Could not find a valid Docker environment`

O Docker CLI faz a mesma chamada mas com credenciais no header → recebe resposta válida.

---

## O que o usuário precisa fazer (uma vez)

**Pré-requisito antes de finalizar a task:**

1. Feche o que estiver usando Docker (pode parar containers primeiro se quiser)
2. Abra **Docker Desktop** → clique no ícone de engrenagem (**Settings**)
3. Vá em **General**
4. Marque a opção **"Expose daemon on tcp://localhost:2375 without TLS"**
5. Clique **"Apply & Restart"**

Após reiniciar, Docker estará acessível em `tcp://localhost:2375` sem autenticação, que é o modo que Testcontainers usa no Windows.

---

## O que o agente precisa fazer para terminar T-03

Quando o usuário disser que Docker Desktop foi reiniciado e o TCP está habilitado, o agente deve:

### 1. Atualizar `pom.xml` — trocar DOCKER_HOST no Surefire

No bloco `<environmentVariables>` do Surefire plugin, substituir o conteúdo atual por:

```xml
<environmentVariables>
    <!-- Docker Desktop no Windows com TCP sem TLS habilitado -->
    <DOCKER_HOST>tcp://localhost:2375</DOCKER_HOST>
</environmentVariables>
```

### 2. Rodar o teste de smoke

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
cd "D:\FIAP\GS2026-1\PU_backend-java\pulso-java"
mvn test -Dtest=InfraSmokeTest
```

Primeira execução: ~3–5 min (download da imagem `gvenzl/oracle-free:23-slim-faststart`, ~1.5 GB).
Segunda execução: < 30s (container reutilizado).

### 3. Verificar saída esperada

```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

### 4. Commitar

```bash
git add pulso-java/
git commit -m "test(infra): add Testcontainers Oracle base class with static container reuse (task T-03)"
```

---

## Estado atual do repositório

Commits feitos até agora:
- `e4a5420` — T-01: scaffold Spring Boot 3.2
- `4a95500` — T-02: application.properties

Os arquivos de T-03 estão criados mas **não commitados ainda** (ficam para o commit acima quando o teste passar).

---