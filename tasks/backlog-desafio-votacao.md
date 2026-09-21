# Backlog Priorizado — Desafio Votação Cooperativista

**Base:** `docs/01-documento-requisitos.md` (RF01–RF08, RN01–RN08, RNF01–RNF10) do projeto `DESAFIO-DB-votacao-fullstack`.
**Critério de priorização:** dependência técnica primeiro (o que bloqueia o quê), depois obrigatoriedade do MVP vs bônus, depois esforço/risco.
**Snapshot do estado atual:** RF01 tem domínio (`Agenda`, `VotingSession`, `Vote`, `VoteOption`) e `AgendaService` prontos e testados (11 testes unitários, TDD). Nada além disso existe ainda: sem controller, sem Flyway, sem Docker, sem front-end.

Convenção de status: `FAZER` (não iniciado) / `FAZENDO` (em andamento) / `FEITO` (concluído) / `BLOQUEADO` (impedido por dependência ou decisão pendente). Atualizar sempre que o status de uma task mudar. Tamanho estimado: S (curto), M (médio), L (grande).

---

## P0 — Fluxo núcleo de votação (bloqueia a entrega do desafio)

1. **[FEITO]** RF01 — Domínio + `AgendaService` (validação via Bean Validation, persistência) — *feito nesta sessão*
2. **[FEITO]** RF01 — Fechar ponta a ponta: `AgendaController` (`POST /api/v1/agendas`), `AgendaResponse`, e o primeiro `GlobalExceptionHandler` (mapeando `InvalidAgendaException` → 422). *Tamanho: S. Depende de: item 1.*
3. **[FEITO]** RF02 — Abrir sessão de votação: `VotingSessionRepository`, `VotingSessionService.openSession` (RN02/RN03/RN05 já modelados em `VotingSession`), `VotingSessionController` (`POST /api/v1/agendas/{id}/sessions`), `VotingSessionResponse`. Também adicionado `VotingSessionAlreadyExistsException` (409) — decisão derivada da cardinalidade `Agenda 1 --> 0..1 VotingSession` já registrada no diagrama, não uma RN explícita numerada. *Tamanho: M. Depende de: item 2 (precisa de `AgendaRepository`/`AgendaNotFoundException`).*
4. **[FEITO]** RF03 + RF04 — Registrar voto: `VoteRepository`, `VoteService.registerVote` (checa `AgendaRepository` → 404, `VotingSessionService.isSessionOpen` para RN02/RN04 → 422, `existsByAgendaIdAndMemberId` como fast-path e `DataIntegrityViolationException` da constraint `UNIQUE` como garantia real contra corrida, ambos → `DuplicateVoteException` 409/RN01), `VotingSessionClosedException`, `VoteController` (`POST /api/v1/agendas/{id}/votes`). `RegisterVoteRequest` ainda sem campo `cpf` de propósito (fica para o item 12, RF06). *Tamanho: M. Depende de: item 3.*
5. **[FEITO]** RF05 — Apurar resultado: `VotingResultService.getResult` (contagem SIM/NAO + flag de sessão encerrada, RN06), endpoint `GET /api/v1/agendas/{id}/results` (no mesmo `VoteController`, path a nível de método já que `/results` é irmão de `/votes`, não aninhado). *Tamanho: S. Depende de: item 4.*

## P1 — Infraestrutura para o MVP rodar de verdade

6. **[FEITO]** RNF01 — Migrations Flyway (`V1__create_agendas_table`, `V2__create_voting_sessions_table`, `V3__create_votes_table`, com `UNIQUE(agenda_id, member_id)`) + `application.properties` apontando pra PostgreSQL via env vars (RN08/RNF10) + `src/test/resources/application.properties` isolando os testes em H2 sem Flyway (`ddl-auto=create-drop`). **Ressalva:** este ambiente não tem Docker nem `psql` disponíveis — as migrations foram revisadas manualmente mas nunca rodaram contra um Postgres real; `mvn test` passa (39/39) porque os testes usam H2 com Flyway desabilitado. Validar de fato quando o item 7 (docker-compose) subir um Postgres real. *Tamanho: M. Depende de: item 5.*
7. **[BLOQUEADO]** RF08 + RNF09 + RNF10 — `Dockerfile`, `docker-compose.yml` (rede dedicada, volume nomeado, healthcheck no Postgres, `depends_on: condition: service_healthy`) e `.env.example` foram escritos e revisados manualmente (`.env` real adicionado ao `.gitignore`). — **Motivo do bloqueio:** este ambiente não tem Docker instalado (`docker: command not found`), então não consigo rodar `docker compose up --build` pra confirmar que a imagem builda, o healthcheck funciona e a app conecta no Postgres. Preciso que você rode isso localmente e me diga se algo falhar. *Tamanho: M. Depende de: item 6.*
8. **[BLOQUEADO]** RNF06 (parte 1) — README com instruções completas de execução (subir via Docker, variáveis de ambiente, como rodar os testes). — **Motivo do bloqueio:** depende do item 7, que está BLOQUEADO (Docker não validado neste ambiente) — documentar "como subir via Docker" antes de confirmar que o Docker sobe seria arriscar instruções erradas no README. *Tamanho: S. Depende de: item 7.*

## P2 — Qualidade e acabamento do MVP

9. **[FEITO]** RNF05 — `VotingFlowIntegrationTest` (`@SpringBootTest` + `@AutoConfigureMockMvc`, H2 real via schema gerado por JPA) cobrindo pauta → sessão → voto → resultado, segunda sessão rejeitada (409), voto duplicado rejeitado pela constraint real (409), 404 em agenda inexistente, 422 em título em branco e em voto com sessão fechada. *Tamanho: M. Depende de: item 6.*
10. **[FEITO]** RNF06 (parte 2) — `springdoc-openapi-starter-webmvc-ui:2.8.6` (Swagger UI em `/swagger-ui.html`, doc em `/v3/api-docs`), validado por `OpenApiDocsTest` (confirma os 3 paths expostos). Sem release oficialmente certificado ainda pra Spring Boot 4/Spring Framework 7, mas testado empiricamente e funcionando. *Tamanho: S. Depende de: itens 2–5 (endpoints existirem).*
11. **[BLOQUEADO]** RNF07 — Front-end responsivo: telas mínimas de cadastrar pauta, abrir sessão, votar e ver resultado. — **Motivo do bloqueio:** nenhum documento do projeto (`docs/01`, `docs/02`, README) fixa o framework de front-end — não é uma decisão técnica que eu deva tomar sozinho (React? Angular? Vue? só HTML/JS simples?). Preciso que você decida antes de eu montar o projeto. *Tamanho: L. Depende de: itens 2–5 (API estável).*

## P3 — Bônus (avaliados à parte, conforme o próprio enunciado)

12. **[FEITO]** RF06 + RN07 — `CpfValidationClient`/`FakeCpfValidationClientAdapter` em `infra/client` (fake local, sem chamada HTTP real — o diagrama já mostrava a classe sem dependência de HTTP), integrado ao `VoteService.registerVote` (agora recebe `cpf`). **Regra do fake, documentada e arbitrária** (não há CPF real por trás): CPF precisa ter 11 dígitos (senão `InvalidCpfException` → 404); dígito final par → `ABLE_TO_VOTE`, ímpar → `UNABLE_TO_VOTE` (`UnableToVoteException` → 422). `RegisterVoteRequest` ganhou o campo `cpf`. *Tamanho: S. Depende de: item 4.*
13. **[FAZER]** RNF02 (bônus 2) — Testes de performance com centenas de milhares de votos, validando índice/constraint sem N+1. *Tamanho: L. Depende de: itens 6, 9.*

**Nota sobre RF07 (versionamento de API):** apesar de listado como bônus no enunciado, RNF04 já fixa a decisão (`/api/v1/...`). Por ser praticamente gratuito, não entra como item separado — todo controller novo (itens 2, 3, 4, 5) já nasce sob `/api/v1` desde a criação, em vez de ser retrofitado depois.
