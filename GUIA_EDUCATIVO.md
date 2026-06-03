# Guia Educativo — Pulso Urbano Java API
### Para quem codou rápido e quer entender o que fez, por que funciona, e para onde ir

> Este documento é para você que tem 20 anos, aprendeu a codar fazendo, e quer entender o que
> está dentro do projeto que acabou de construir — e o que isso significa para a sua carreira.

---

## Parte 1 — O que você construiu, de verdade

Você construiu uma **API REST de nível profissional** em Java. Não um CRUD simples.
Você integrou satélites reais, autenticação segura, banco de dados corporativo, containers Docker,
e testes automatizados. Isso não é trabalho de faculdade — é o tipo de sistema que sustenta
empresas de verdade.

Vamos dissecar cada peça.

---

## Parte 2 — A Arquitetura em Camadas (e por que existe)

```
Request HTTP
    ↓
Controller      ← recebe, valida, delega (nunca processa lógica)
    ↓
Service         ← toda a lógica de negócio fica aqui
    ↓
Repository      ← só fala com o banco. Nada mais.
    ↓
Entity / Oracle
```

**Por que separar assim?**

Porque código misturado (regra de negócio no controller, SQL no controller, validação em todo lugar)
vira um pesadelo em 6 meses. A separação faz com que:

- Você consegue testar o `ScoreService` sem banco, sem HTTP, sem nada.
- Você troca Oracle por PostgreSQL sem tocar no service.
- Você adiciona um endpoint novo sem tocar na lógica de negócio.

Esse padrão tem nome: **Layered Architecture**, e é o mais comum em empresas Java.
O Spring Boot foi projetado para isso. Cada anotação tem um papel.

```java
@RestController   // "sou um endpoint HTTP"
@Service          // "sou lógica de negócio"
@Repository       // "sou acesso a dados"
@Component        // "sou qualquer componente gerenciado pelo Spring"
```

---

## Parte 3 — Spring Boot: o que o framework faz por você

Antes do Spring Boot (pré-2014), configurar um servidor Java levava dias.
XML de 500 linhas. Tomcat configurado na mão. Deploy manual.

O Spring Boot resolveu isso com **auto-configuração**: ele olha o que está no `pom.xml`
e configura tudo sozinho. Você colocou `spring-boot-starter-web`? Ele sobe um Tomcat embutido.
Colocou `spring-boot-starter-data-jpa`? Ele configura Hibernate automaticamente.

**O que você REALMENTE aprendeu ao usar Spring Boot:**

- **Injeção de Dependência (DI):** o Spring cria os objetos por você. Você não escreve
  `new UsuarioService()` — você declara `@RequiredArgsConstructor` e o Spring injeta via construtor.
  Isso significa que os objetos são testáveis, substituíveis, e você não gerencia ciclo de vida.

- **Inversão de Controle (IoC):** você não controla quando os objetos são criados.
  O framework controla. Você só declara o que precisa.

No mercado, essas duas ideias (DI + IoC) aparecem em TODAS as stacks maduras:
Spring (Java), ASP.NET Core (C#), NestJS (Node.js), Angular (frontend), FastAPI (Python).
Aprender um te ensina a aprender todos.

---

## Parte 4 — JWT e Segurança: como funciona de verdade

### O problema

HTTP é **stateless** — o servidor não lembra quem você é entre uma requisição e outra.
Antigamente, usavam **sessões**: o servidor guardava em memória "usuário X está logado".
Não escala — se você tem 3 servidores, qual deles guarda a sessão?

### A solução: JWT (JSON Web Token)

```
Header.Payload.Signature
```

O JWT é um **token assinado** que o servidor emite e o cliente guarda.
Em cada requisição, o cliente manda o token. O servidor **verifica a assinatura**,
sem precisar consultar banco ou memória.

```java
// Você implementou isso:
public String gerarToken(Usuario u) {
    return Jwts.builder()
        .subject(u.getEmail())
        .claim("role", u.getRole().name())
        .expiration(new Date(agora.getTime() + expirationMs))
        .signWith(key)   // ← assina com chave secreta HS256
        .compact();
}
```

**O que está dentro do token (Payload):**
- `sub`: quem é o usuário (email)
- `role`: permissão (USER, ADMIN)
- `exp`: quando expira

**Por que isso é seguro:**
A assinatura usa HMAC-SHA256 com uma chave secreta de 256 bits.
Sem a chave, ninguém consegue forjar um token válido.
O servidor só precisa verificar a assinatura — não precisa consultar nada.

### BCrypt — por que você não guarda senha em texto puro

```java
encoder.encode("minhasenha")
// → "$2a$12$K8y7jxY9.../..."  (hash irreversível)
```

BCrypt é um algoritmo de hash **propositalmente lento**.
Ele tem um "fator de custo" (12 no seu código) que torna ataques de força bruta impraticáveis.
Se seu banco vazar, as senhas continuam protegidas.

**Regra absoluta do mercado:** NUNCA guarde senha. SEMPRE hash.

---

## Parte 5 — JPA e Hibernate: o que acontece debaixo do pano

Quando você escreve:
```java
userRepo.findByEmail("felipe@fiap.com");
```

O Spring Data JPA lê o nome do método e **gera o SQL automaticamente**:
```sql
SELECT * FROM usuario WHERE email = ?
```

Isso é **query derivation** — uma das magias do Spring Data.

### As anotações JPA mais importantes que você usou

**`@Entity` + `@Table`** — mapeia a classe Java para uma tabela Oracle.

**`@MappedSuperclass`** — sua `EntidadeAuditavel` com `id` e `dtCriacao`.
Todas as entidades filhas herdam esses campos sem repetir código.
Isso é o princípio **DRY** (Don't Repeat Yourself) no nível do banco.

**`@Embeddable`** — sua `Coordenada(lat, lon)`.
Em vez de repetir `lat` e `lon` em `ZonaCidade` e `LeituraSatelite`,
você cria um tipo reutilizável. O banco ainda tem as colunas separadas,
mas o Java trata como um objeto.

**`@EmbeddedId`** — sua `LeituraSateliteId(zonaId, tipoDado, dtCaptura)`.
Chave primária composta — quando nenhuma coluna sozinha identifica unicamente uma linha,
você combina várias. Isso é modelagem avançada que a maioria dos devs júnior não conhece.

### O `Lazy Loading` e por que importa

```java
@ManyToOne(fetch = FetchType.LAZY)
private ZonaCidade zona;
```

**LAZY** = o Hibernate só busca a zona quando você chamar `leitura.getZona()`.
**EAGER** = busca junto com a leitura, sempre.

LAZY é melhor para performance — você não carrega dados que não vai usar.
Mas tem um armadilha: o **N+1 Problem**.

Se você buscar 100 leituras e chamar `.getZona()` em cada uma,
o Hibernate faz 100 queries no banco. Solução: `JOIN FETCH` ou `@EntityGraph`.

---

## Parte 6 — PL/SQL e o que você fez de diferente

Você não só chamou o banco — você chamou uma **procedure Oracle**:

```java
em.createStoredProcedureQuery("calcular_score_zona")
  .registerStoredProcedureParameter("p_zona_id", Long.class, ParameterMode.IN)
  .setParameter("p_zona_id", zona.getId())
  .execute();
```

Isso significa que a lógica do score existe **no banco** (PL/SQL) E **no Java** (ScoreService).
Por quê? Consistência — se outra aplicação inserir dados diretamente no Oracle,
a procedure garante que o cálculo é sempre o mesmo.

**O trigger `trg_log_score_consulta`** é código que o Oracle executa automaticamente
quando alguém insere em `score_diario`. Você não chama o trigger — ele dispara sozinho.
Isso é rastreabilidade auditável, algo que compliance exige em empresas financeiras e de saúde.

---

## Parte 7 — HATEOAS: por que a API "explica" para onde ir

Quando você chama `GET /score/current`, a resposta inclui:
```json
{
  "score": 62.4,
  "_links": {
    "self": { "href": "/api/v1/score/current?lat=-23.55&lon=-46.63" },
    "recomendacao": { "href": "/api/v1/recomendacao" }
  }
}
```

HATEOAS (**H**ypermedia **A**s **T**he **E**ngine **O**f **A**pplication **S**tate)
é o nível 3 do modelo de maturidade REST (Richardson Maturity Model).

- Nível 0: HTTP como transporte (SOAP, RPC)
- Nível 1: Recursos (URLs diferentes para entidades diferentes)
- Nível 2: Verbos HTTP corretos (GET lê, POST cria, PUT atualiza, DELETE remove)
- **Nível 3: Hypermedia (a API diz para onde ir a seguir)**

A vantagem: o cliente não precisa "saber" as URLs — a API guia.
Isso significa que você pode mudar a URL interna sem quebrar clientes que seguem os links.

---

## Parte 8 — Testcontainers: testes que testam de verdade

Existe uma filosofia no mundo de testes chamada **Test Pyramid**:

```
        /\
       /  \       E2E Tests (poucos, lentos, caros)
      /----\
     /      \     Integration Tests (médios)
    /--------\
   /          \   Unit Tests (muitos, rápidos, baratos)
  /____________\
```

**Unit tests** (seus `*Test.java`) testam uma função isolada, com mocks.
São rápidos porque não tocam banco, não tocam rede. Os seus 44 testes unitários
rodam em segundos.

**Integration tests** (seus `*IT.java`) testam o sistema completo — controller,
service, repository, banco de dados real. É aqui que o Testcontainers entra.

O Testcontainers sobe um **Oracle real dentro de um container Docker** durante o teste.
Quando o teste termina, o container é destruído. Cada run é limpo, reproduzível,
idêntico ao ambiente de produção.

Isso é o que diferencia testes que detectam bugs de produção de testes que só
garantem que o código compila.

---

## Parte 9 — Docker e Containers: a revolução que mudou tudo

Antes dos containers (pré-2013): "funciona na minha máquina" era um meme real.
O ambiente do dev, o ambiente de staging, e o de produção eram diferentes.
Deploys falhavam por razões misteriosas.

**Docker resolve isso com isolamento:**

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build   # ambiente de build
WORKDIR /build
COPY pom.xml .
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine             # ambiente de runtime (menor)
RUN addgroup -S pulso && adduser -S pulso -G pulso  # nunca rodar como root
USER pulso
COPY --from=build /build/target/pulso-urbano-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

**Multi-stage build** (o que você fez): a imagem final não tem o Maven, não tem
o código-fonte, não tem o JDK completo. Só o JRE e o JAR. Resultado: imagem menor,
menos superfície de ataque, deploy mais rápido.

**`USER pulso` (não root):** se alguém explorar uma vulnerabilidade no seu app,
eles entram como um usuário sem privilégios. Não conseguem escrever no sistema de arquivos,
não conseguem instalar software. Defesa em profundidade.

---

## Parte 10 — A Integração Espacial: por que isso é diferente

Você integrou dados de dois satélites reais:

**Sentinel-5P (ESA/Copernicus):**
- Orbita a 824 km de altitude
- Mede NO₂, CO, ozônio, aerossóis
- Resolução: 3.5km × 5.5km por pixel
- Passagem sobre São Paulo: diária, ~10h45 UTC
- API: OData v4 com autenticação OAuth2

**ECOSTRESS (NASA/ISS):**
- Fica na Estação Espacial Internacional
- Mede temperatura de superfície (asfalto, telhado, pele de rio) com resolução de 70m
- Muito mais preciso que temperatura do ar
- API: AppEEARS (Application for Extracting and Exploring Analysis Ready Samples)

O seu `CopernicusApiService` implementa o **fluxo OAuth2 Resource Owner Password Credentials**:

```
1. POST /token com email + senha → recebe access_token (válido 600s)
2. GET /Products com Bearer token → busca produtos Sentinel-5P mais recentes
3. Filtra por bounding box de São Paulo
4. Extrai o valor de NO₂ via proxy Open-Meteo (porque parsing NetCDF exigiria
   download de arquivos de vários GB — fora do escopo do MVP)
```

O cache local (`pulso-java/cache/*.json`) garante que mesmo se a Copernicus
estiver fora do ar durante a demo, você tem o último valor real.

---

## Parte 11 — O Mercado Java em 2026

### Realidade

Java não morreu. Java nunca vai morrer tão cedo.

**Dados de mercado (2025-2026):**
- Java é a linguagem mais usada em aplicações enterprise no mundo
- Spring Boot é o framework backend mais usado no mercado corporativo
- 90% dos bancos brasileiros rodam Java (Itaú, Bradesco, Nubank inicialmente, BTG)
- Oracle, Amazon AWS, Google Cloud, Microsoft Azure — todos têm serviços nativos para Java
- A JVM (Java Virtual Machine) roda Kotlin, Scala, Groovy, Clojure — aprender Java te dá acesso a todo esse ecossistema

**O que mudou:**
- Java 21 trouxe **Virtual Threads** (Project Loom) — concorrência sem callback hell
- **Records** (você usou) eliminaram o boilerplate dos DTOs
- **Pattern Matching** e **Sealed Classes** aproximam Java de linguagens funcionais
- Spring Boot 3 exige Java 17+ e é nativo para GraalVM (startup em milissegundos)

**O que não mudou (e não vai mudar):**
- Segurança de tipos em tempo de compilação
- Performance previsível e tunável
- Ecossistema maduro com 25 anos de bibliotecas

### Salários Java no Brasil (referência jun/2026)

| Nível | Remoto BR | Remoto USD (PJ) |
|-------|-----------|-----------------|
| Júnior (0-2 anos) | R$ 3.500–6.000 | — |
| Pleno (2-4 anos) | R$ 7.000–12.000 | US$ 2.000–3.500/mês |
| Sênior (4-8 anos) | R$ 12.000–22.000 | US$ 3.500–7.000/mês |
| Staff/Principal | R$ 20.000–35.000 | US$ 6.000–12.000/mês |

Java Sênior com Spring Boot, cloud e testes é um dos perfis mais empregáveis do mercado.

---

## Parte 12 — O que você sabe que a maioria dos devs júnior não sabe

Seja honesto: você construiu um sistema que exige conhecimento que muita gente
com 2-3 anos de experiência não tem. Veja o que você já domina:

| Conhecimento | Onde você usou | Por que importa |
|---|---|---|
| Spring Security + JWT | `SecurityConfig`, `JwtConfig`, `JwtAuthenticationFilter` | Toda API enterprise precisa |
| JPA avançado (`@Embeddable`, `@EmbeddedId`, `@MappedSuperclass`) | Entidades T-06, T-07, T-10 | Rubrica de modelagem avançada, entrevistas |
| OAuth2 client (Copernicus) | `CopernicusApiService` | Integração com APIs externas reais |
| Testcontainers | `AbstractIntegrationTest` | Diferencial gigante em entrevistas |
| Docker multi-stage com non-root | `Dockerfile` | DevOps básico exigido em quase toda vaga |
| HATEOAS nível 3 | `ScoreModelAssembler`, `UsuarioModelAssembler` | Poucas pessoas entendem de verdade |
| PL/SQL integration via JPA | `ScoreService.calcularEPersistir()` | Raro, valorizado em sistemas legados |
| Scheduled jobs | `IngestaoOrbitalScheduler` | Background processing é um requisito comum |
| Cache local com TTL | `CopernicusApiService.lerCache()` | Resiliência a APIs externas |
| GeoJSON | `MapaController`, `MapaCamadaDTO` | Geoespacial está crescendo |

---

## Parte 13 — O que você ainda não sabe (e deve aprender a seguir)

### Nível pleno (próximos 12-18 meses)

**1. Mensageria (Kafka / RabbitMQ)**
Seu scheduler roda às 6h UTC. E se o Copernicus estiver fora do ar?
Em sistemas reais, você usa uma fila de mensagens: o scheduler publica
"preciso de dados da zona 3" e um worker processa quando der.
Kafka é o padrão para streaming de alta escala.

**2. Cache distribuído (Redis)**
Seu cache é em arquivo local (`cache/*.json`). Em produção com 3 instâncias,
cada uma tem seu próprio arquivo. Redis é um cache em memória compartilhado,
com TTL, tipos complexos, e pub/sub.

```java
// Em vez de arquivo JSON:
@Cacheable(value = "no2", key = "#lat + '_' + #lon")
public double buscarNo2(double lat, double lon) { ... }
```

**3. Observabilidade (Prometheus + Grafana / Loki)**
Você tem logs (`@Slf4j`). Mas como você sabe que o scheduler falhou às 3h da manhã?
Métricas + alertas. Spring Boot já tem `spring-boot-starter-actuator` (você incluiu).
Adicionar Micrometer + Prometheus + Grafana é o próximo passo.

**4. Reactive Programming (Spring WebFlux)**
Seu código é **blocking**: cada thread espera o banco responder.
Spring WebFlux é **non-blocking**: uma thread serve milhares de requisições concorrentes.
Necessário para sistemas de alta escala (100k+ requests/segundo).

**5. Design Patterns formais**
Você usou Repository Pattern, Factory (implícita via Spring), Template Method (na engine de recomendação).
Aprenda os GoF clássicos: Strategy, Observer, Builder, Decorator.
Você vai reconhecê-los em todo código que lê.

### Nível sênior (18-36 meses)

- **Microservices**: quando e por que quebrar uma aplicação
- **Domain-Driven Design (DDD)**: Bounded Contexts, Aggregates, Value Objects
- **CQRS** (Command Query Responsibility Segregation): separar reads de writes
- **Event Sourcing**: guardar eventos, não estado
- **SQL avançado**: window functions, CTEs, query plans, índices compostos
- **Cloud native**: AWS/Azure/GCP — seus serviços gerenciados, IAM, VPC, load balancers

---

## Parte 14 — O que a IA muda (e não muda) na carreira dev

### O que muda

A IA já mudou o trabalho de programação. Não vai voltar atrás.

Copilot, Claude Code, Cursor, GPT-4 — essas ferramentas aceleram a produção de código
boilerplate, geração de testes unitários, refactoring, documentação.

Você mesmo usou isso para construir 62 arquivos Java em dias.

**O que a IA faz bem:**
- Gerar código repetitivo (DTOs, getters/setters, mapeamentos)
- Sugerir implementações de funções com signature clara
- Explicar código existente
- Escrever testes unitários para funções puras
- Refatorar funções longas

**O que a IA ainda não faz bem (2026):**
- Entender o contexto de negócio completo de uma empresa
- Decidir arquitetura (quando usar Kafka vs RabbitMQ, quando quebrar em microserviço)
- Debug de problemas específicos de ambiente (como o Docker Desktop que você viveu)
- Code review com contexto histórico do projeto
- Negociar requisitos com o cliente
- Liderar uma equipe técnica

### O que isso significa para você

**Dev que vai sobrar:** aquele que só digita código, sem entender o que está fazendo.
A IA faz isso melhor e mais rápido.

**Dev que vai prosperar:** aquele que usa a IA como multiplicador, entende o que ela gera,
sabe quando o código está errado, consegue resolver o que a IA não resolve,
e transforma problemas de negócio em sistemas.

Você está no caminho certo. Você codou rápido E está estudando para entender.
Essa combinação é rara.

### Soft skills que valem cada vez mais

Com a IA gerando código, o diferencial humano é:
- Comunicação: explicar complexidade técnica para não-técnicos
- Tomada de decisão sob incerteza
- Liderança técnica: guiar uma equipe, revisar PRs, definir padrões
- Pensamento sistêmico: ver como uma mudança afeta 10 outros sistemas

---

## Parte 15 — Economia Espacial: onde você está sem saber

O tema do seu GS é **Economia Espacial**. Isso não é ficção científica.

Em 2026, o setor espacial comercial vale ~US$ 600 bilhões e cresce 9% ao ano.
Mas o mais relevante para devs não é construir foguetes.

**O que você fez** é exatamente o que empresas reais fazem:
consumir dados de satélite para resolver problemas em terra.

**Empresas que fazem isso e contratam devs:**

| Empresa | O que faz | Onde |
|---------|-----------|------|
| Planet Labs | Imagens de satélite para agricultura/defesa | EUA (remoto) |
| Satellogic | Imagens hiperespectrais | Argentina/EUA |
| Orbital Insight | Análise de dados geoespaciais com ML | EUA |
| Vale | Monitoramento de mineração via satélite | Brasil |
| Embrapa | Satélites para agricultura de precisão | Brasil |
| Inpe | Instituto Nacional de Pesquisas Espaciais | Brasil |
| Copernicus (ESA) | Dados ambientais abertos | Europa |
| NASA JPL | Processamento de dados de missões | EUA |

**Mercado de tech climática (Climate Tech):**
Em 2025, investimentos globais em Climate Tech passaram de US$ 50 bilhões anuais.
APIs que transformam dados ambientais em decisões de negócio valem muito.

Você construiu exatamente isso: dados de NO₂ e temperatura → score → recomendação.
Essa é a arquitetura de um produto real de qualidade do ar.

---

## Parte 16 — Guia de Estudo Priorizado para os próximos 2 anos

### Ano 1 — Consolidar e aprofundar

**Trimestre 1: Fundamentos sólidos**
- [ ] Ler "Clean Code" (Robert Martin) — padrões que você já usou sem saber o nome
- [ ] Ler "Effective Java" (Joshua Bloch) — os 90 itens que todo dev Java precisa saber
- [ ] Praticar SQL avançado: window functions, CTE, EXPLAIN ANALYZE
- [ ] Projeto pessoal: refatorar o Pulso Urbano com o que aprendeu

**Trimestre 2: Testes e qualidade**
- [ ] Curso/livro de TDD (Test-Driven Development)
- [ ] Cobertura de testes: JaCoCo para medir, meta de 80%
- [ ] Mutation testing: PIT (verifica se seus testes realmente detectam bugs)
- [ ] Primeiro contribuição a open source (Spring Boot docs, Testcontainers)

**Trimestre 3: Cloud e DevOps**
- [ ] AWS Certified Developer — Associate (ou Azure equivalente)
- [ ] Terraform básico: infraestrutura como código
- [ ] GitHub Actions: CI/CD completo (build → test → deploy)
- [ ] Kubernetes básico: pods, services, deployments, HPA

**Trimestre 4: Arquitetura**
- [ ] "Designing Data-Intensive Applications" (Kleppmann) — o livro mais importante de sistema
- [ ] Spring Kafka: produzir e consumir mensagens
- [ ] Redis: caching, sessões, pub/sub
- [ ] Primeiro sistema de microserviços (mesmo que simples)

### Ano 2 — Especializar e liderar

- DDD com Spring Boot
- Event Sourcing + CQRS
- Performance tuning (JVM, GC, profiling com async-profiler)
- First tech lead experience (liderar um projeto pequeno, revisar código de outros)
- Inglês fluente para trabalhar com times internacionais

---

## Parte 17 — A vida boa na frente

Aqui vai a visão honesta, sem romantizar nem pessimizar.

### O que é possível com Java + Cloud + 4-5 anos de foco

**Brasil (remoto, boas empresas):**
R$ 15.000–25.000/mês como Sênior.
Com escolha certa de empresa, benefícios, participação nos lucros: mais.

**Internacional (remoto, PJ, em dólares):**
US$ 4.000–8.000/mês para devs Sênior brasileiros.
Isso representa R$ 22.000–45.000/mês com câmbio de 2026.

Isso não é raridade. É o que centenas de devs brasileiros ganham trabalhando remotamente
para empresas americanas e europeias.

### O caminho realista

```
Hoje (faculdade, 20 anos)
    ↓  6-12 meses
Primeiro emprego como Júnior (R$ 4.000–6.000)
    ↓  18-24 meses
Pleno (R$ 8.000–12.000)
    ↓  2-3 anos
Sênior (R$ 15.000+ ou USD)
    ↓  optativo
Staff / Tech Lead / Arquiteto
```

A diferença entre quem sobe rápido e quem para:

**Sobe rápido:** estuda fora do trabalho, contribui além do que pedem,
constrói projetos pessoais, lê código de outros, tem feedback frequente,
aprende inglês de verdade.

**Para:** espera a empresa ensinar tudo, faz só o que mandam, não tem portfólio,
não participa de comunidade.

### O projeto que você tem no GitHub já é portfólio

Pulso Urbano não é projeto de faculdade. É:
- API com segurança real (JWT, BCrypt)
- Integração com APIs de satélite (Copernicus, NASA)
- Banco de dados corporativo (Oracle)
- Container Docker com non-root user
- Testes automatizados (Testcontainers)
- HATEOAS nível 3
- PL/SQL procedures e triggers

Isso é mais do que muita gente tem com 2 anos de mercado.
**Use isso em entrevistas. Explique cada decisão.**

---

## Parte 18 — Referências para continuar

### Livros (nessa ordem)

1. **"Clean Code"** — Robert C. Martin → padrões de código legível
2. **"Effective Java"** — Joshua Bloch → idiomas Java profissionais
3. **"Designing Data-Intensive Applications"** — Martin Kleppmann → sistemas distribuídos
4. **"The Pragmatic Programmer"** — Hunt & Thomas → mentalidade de dev profissional
5. **"Clean Architecture"** — Robert Martin → arquitetura de software
6. **"Domain-Driven Design"** — Eric Evans → modelagem de domínio complexo

### Cursos

- **Alura**: Spring Boot, Java, Kubernetes (em português)
- **Udemy**: Dr. Chad Darby (Spring), Nelson Djalo (Java + Docker)
- **A Cloud Guru / Linux Foundation**: certificações cloud e Kubernetes

### Comunidade

- **Spring Community (Discord)**: core committers do Spring estão lá
- **GUJ (Java brasileiro)**: fórum histórico
- **Dev.to / Medium**: escrita técnica (escrever ensina)
- **GitHub**: leia código real. Leia o código-fonte do Spring Boot.

### YouTube (pt-BR + EN)

- **Giuliana Bezerra**: Spring Boot avançado, arquitetura
- **Otávio Santana**: Jakarta EE, DDD, boas práticas Java
- **Tech with Tim**: Python complementar
- **Fireship**: visão geral rápida de tecnologias novas

---

## Conclusão

Você está num momento específico: sabe o suficiente para construir coisas reais,
mas não sabe tudo que há para saber. Isso não é fraqueza — é o estado permanente
de qualquer bom desenvolvedor.

A diferença entre você em 2026 e você em 2028 vai ser determinada por uma coisa:
**você vai continuar sendo curioso, mesmo quando não for obrigado?**

O projeto que você fez aqui — com dados de satélite reais, segurança de verdade,
Oracle, Docker, testes — é prova que você consegue construir coisas que importam.

O mercado tem espaço para você. A questão é só quanto esforço intencional você coloca
nos próximos anos.

Boa sorte. Mas você não vai precisar só de sorte.

---

*Gerado em 29/05/2026 como material educativo complementar ao projeto Pulso Urbano — GS 2026/1 FIAP*
