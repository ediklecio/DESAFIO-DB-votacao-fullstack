# Backlog Priorizado — Desafio Votação Cooperativista

**Base:** `docs/01-documento-requisitos.md` (RF01–RF08, RN01–RN08, RNF01–RNF10) do projeto `DESAFIO-DB-votacao-fullstack`.
**Critério de priorização:** dependência técnica primeiro (o que bloqueia o quê), depois obrigatoriedade do MVP vs bônus, depois esforço/risco.
**Snapshot do estado atual:** RF01 tem domínio (`Agenda`, `VotingSession`, `Vote`, `VoteOption`) e `AgendaService` prontos e testados (11 testes unitários, TDD). Nada além disso existe ainda: sem controller, sem Flyway, sem Docker, sem front-end.

Convenção de status: `FAZER` (não iniciado) / `FAZENDO` (em andamento) / `FEITO` (concluído) / `BLOQUEADO` (impedido por dependência ou decisão pendente). Atualizar sempre que o status de uma task mudar. Tamanho estimado: S (curto), M (médio), L (grande).

---

## P0 — Fluxo núcleo de votação (bloqueia a entrega do desafio)

1. **[FEITO]** RF01 — Domínio + `AgendaService` (validação via Bean Validation, persistência) — *feito nesta sessão*
2. **[FEITO]** RF01 — Fechar ponta a ponta: `AgendaController` (`POST /api/v1/agendas`), `AgendaResponse`, e o primeiro `GlobalExceptionHandler` (mapeando `InvalidAgendaException` → 422). *Tamanho: S. Depende de: item 1.*
3. **[FAZER]** RF02 — Abrir sessão de votação: `VotingSessionRepository`, `VotingSessionService.openSession` (RN02/RN03/RN05 já modelados em `VotingSession`), `VotingSessionController` (`POST /api/v1/agendas/{id}/sessions`), `VotingSessionResponse`. *Tamanho: M. Depende de: item 2 (precisa de `AgendaRepository`/`AgendaNotFoundException`).*
4. **[FAZER]** RF03 + RF04 — Registrar voto: `VoteRepository`, `VoteService.registerVote` (usa `VotingSessionService.isSessionOpen` para RN04, captura violação da constraint `UNIQUE` de `Vote` e relança `DuplicateVoteException` para RN01), `VotingSessionClosedException`, `VoteController` (`POST /api/v1/agendas/{id}/votes`). Atualizar `GlobalExceptionHandler` com os novos mapeamentos (409 duplicidade, 422/400 sessão fechada). *Tamanho: M. Depende de: item 3.*
5. **[FAZER]** RF05 — Apurar resultado: `VotingResultService.getResult` (contagem SIM/NAO + flag de sessão encerrada, RN06), endpoint `GET /api/v1/agendas/{id}/results`. *Tamanho: S. Depende de: item 4.*

## P1 — Infraestrutura para o MVP rodar de verdade

6. **[FAZER]** RNF01 — Migrations Flyway (`agendas`, `voting_sessions`, `votes`, incluindo a constraint `UNIQUE(agenda_id, member_id)`) + profile PostgreSQL (H2 continua só para testes). *Tamanho: M. Depende de: item 5 (schema estável) — mas pode começar em paralelo assim que as entidades do item 4 estiverem fechadas.*
7. **[FAZER]** RF08 + RNF09 + RNF10 — `Dockerfile` da aplicação, `docker-compose.yml` (rede dedicada, volume nomeado, healthcheck no Postgres, `depends_on: condition: service_healthy`), `.env` / `.env.example`. *Tamanho: M. Depende de: item 6.*
8. **[FAZER]** RNF06 (parte 1) — README com instruções completas de execução (subir via Docker, variáveis de ambiente, como rodar os testes). *Tamanho: S. Depende de: item 7.*

## P2 — Qualidade e acabamento do MVP

9. **[FAZER]** RNF05 — Testes de integração (`@SpringBootTest` / `@DataJpaTest`) cobrindo o fluxo pauta → sessão → voto → resultado, incluindo o caso de voto duplicado batendo na constraint real. *Tamanho: M. Depende de: item 6.*
10. **[FAZER]** RNF06 (parte 2) — Swagger/OpenAPI documentando os endpoints. *Tamanho: S. Depende de: itens 2–5 (endpoints existirem).*
11. **[FAZER]** RNF07 — Front-end responsivo: telas mínimas de cadastrar pauta, abrir sessão, votar e ver resultado. *Tamanho: L. Depende de: itens 2–5 (API estável).*

## P3 — Bônus (avaliados à parte, conforme o próprio enunciado)

12. **[FAZER]** RF06 + RN07 — Client fake de validação de CPF (`CpfValidationClient`/`FakeCpfValidationClientAdapter`), integrado ao `VoteService` (404 CPF inválido, rejeição se `UNABLE_TO_VOTE`). *Tamanho: S. Depende de: item 4.*
13. **[FAZER]** RNF02 (bônus 2) — Testes de performance com centenas de milhares de votos, validando índice/constraint sem N+1. *Tamanho: L. Depende de: itens 6, 9.*

**Nota sobre RF07 (versionamento de API):** apesar de listado como bônus no enunciado, RNF04 já fixa a decisão (`/api/v1/...`). Por ser praticamente gratuito, não entra como item separado — todo controller novo (itens 2, 3, 4, 5) já nasce sob `/api/v1` desde a criação, em vez de ser retrofitado depois.
