# Guia de Testes da API via Swagger

Passo a passo para testar todas as funcionalidades da API de votação
(cadastro de pauta, abertura de sessão, voto e apuração) usando o
Swagger UI, incluindo os cenários de erro que valem a pena verificar.

---

## Pré-requisito: aplicação no ar

Suba a aplicação por qualquer um dos perfis descritos em
[`docs/INICIALIZACAO.md`](./INICIALIZACAO.md) (Docker Compose ou híbrido).
Os dois expõem a API em `localhost:8080`. Para confirmar que subiu, um
`GET http://localhost:8080/v3/api-docs` deve responder `200 OK` com o
JSON da especificação OpenAPI.

## 1. Acessando o Swagger UI

| Recurso | URL |
|---|---|
| Swagger UI (interface interativa) | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON (spec crua, útil para importar em Postman/Insomnia) | http://localhost:8080/v3/api-docs |
| API (base path) | http://localhost:8080/api/v1 |

Não há autenticação: conforme o enunciado do desafio, a segurança das
interfaces foi abstraída, então toda chamada é tratada como autorizada
— basta abrir o Swagger UI e usar o botão **"Try it out"** em cada
endpoint.

A UI é gerada automaticamente pelo springdoc a partir dos controllers e
DTOs (`org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6`,
`pom.xml:94-98`). Os paths seguem os defaults do springdoc, mas os
grupos (tags) foram renomeados via `@Tag` (`io.swagger.v3.oas.annotations.tags.Tag`)
direto nos controllers, para exibir nomenclatura de negócio em vez do
nome técnico da classe:

| Controller | Tag exibida no Swagger UI |
|---|---|
| `AgendaController` | **Pauta** |
| `VoteController` | **Voto** |
| `VotingSessionController` | **Sessão** |

## 2. Mapa dos endpoints

| Método | Path | O que faz | Status de sucesso |
|---|---|---|---|
| `POST` | `/api/v1/agendas` | Cadastra uma pauta | `201 Created` |
| `POST` | `/api/v1/agendas/{agendaId}/sessions` | Abre a sessão de votação da pauta | `201 Created` |
| `POST` | `/api/v1/agendas/{agendaId}/votes` | Registra o voto de um associado | `201 Created` |
| `GET` | `/api/v1/agendas/{agendaId}/results` | Apura e retorna o resultado da votação | `200 OK` |
| `GET` | `/api/v1/agendas?page=0&size=20` | Lista pautas (mais recentes primeiro) com status da sessão e contagem de votos | `200 OK` |
| `GET` | `/api/v1/agendas/{agendaId}` | Detalha uma pauta com status da sessão, segundos restantes e contagem de votos | `200 OK` |

Os dois `GET` de leitura existem para alimentar o front-end. Exemplo de item
(`sessionStatus` ∈ `NOT_STARTED` · `OPEN` · `CLOSED`, derivado do relógio do
servidor — RN05; `secondsRemaining` só é maior que zero com a sessão aberta):

```json
{
  "id": 1,
  "title": "Reforma do estatuto",
  "description": "Votação sobre a nova redação do estatuto social",
  "sessionStatus": "OPEN",
  "openedAt": "2026-09-22T18:00:00",
  "closesAt": "2026-09-22T18:05:00",
  "secondsRemaining": 240,
  "totalYes": 3,
  "totalNo": 1
}
```

A listagem responde `{ "content": [...], "page", "size", "totalElements", "totalPages" }`.
Sessões e contagens de votos são carregadas em lote (2 queries por página, sem N+1).

## 3. Fluxo completo (happy path)

Execute os passos nessa ordem — cada um depende do `id` retornado pelo
anterior. Em cada endpoint do Swagger UI: expanda o card, clique em
**"Try it out"**, cole o corpo de exemplo (quando houver) e clique em
**"Execute"**.

### 3.1 Cadastrar uma pauta — `POST /api/v1/agendas`

Corpo:

```json
{
  "title": "Aprovação do balanço 2026",
  "description": "Votação sobre o balanço patrimonial do exercício de 2026"
}
```

Resposta esperada — `201 Created`, header `Location:
/api/v1/agendas/{id}` e corpo:

```json
{
  "id": 1,
  "title": "Aprovação do balanço 2026",
  "description": "Votação sobre o balanço patrimonial do exercício de 2026"
}
```

Anote o `id` — ele é o `agendaId` usado nos próximos passos.

### 3.2 Abrir a sessão de votação — `POST /api/v1/agendas/{agendaId}/sessions`

Preencha `agendaId` no path com o `id` do passo anterior. O corpo é
opcional (o controller aceita `@RequestBody(required = false)`):

```json
{
  "durationMinutes": 5
}
```

Se você não enviar corpo nenhum (ou enviar `{}`), a sessão abre com a
duração default de **1 minuto**, conforme a regra do desafio.

Resposta esperada — `201 Created`:

```json
{
  "id": 1,
  "agendaId": 1,
  "openedAt": "2026-09-22T10:00:00",
  "closesAt": "2026-09-22T10:05:00"
}
```

> Dica: para testar rapidamente o cenário de "sessão fechada" (seção 4),
> abra uma sessão sem informar `durationMinutes` e espere ~1 minuto
> antes de votar.

### 3.3 Registrar um voto — `POST /api/v1/agendas/{agendaId}/votes`

Corpo:

```json
{
  "memberId": 1,
  "cpf": "11122233344",
  "voteAnswer": "YES"
}
```

- `voteAnswer` aceita apenas `"YES"` ou `"NO"` (enum `VoteOption`).
- `cpf` precisa ter exatamente 11 dígitos numéricos — veja a regra do
  validador fake de CPF logo abaixo, ela decide se o voto é aceito.

Resposta esperada — `201 Created`:

```json
{
  "id": 1,
  "agendaId": 1,
  "memberId": 1,
  "voteAnswer": "YES",
  "registeredAt": "2026-09-22T10:01:12"
}
```

Repita a chamada com o mesmo `memberId` (ou o mesmo `cpf` com outro
`memberId`) para ver o `409 Conflict` de voto duplicado, descrito na
seção 4.

### 3.4 Apurar o resultado — `GET /api/v1/agendas/{agendaId}/results`

Sem corpo, só o `agendaId` no path. Resposta esperada — `200 OK`:

```json
{
  "agendaId": 1,
  "totalYes": 1,
  "totalNo": 0,
  "sessionClosed": false
}
```

`sessionClosed` só vira `true` depois que `closesAt` (passo 3.2) já
tiver passado.

## 4. Regra do validador fake de CPF (Tarefa Bônus 1)

O voto (passo 3.3) consulta um client fake
(`FakeCpfValidationClientAdapter`,
`src/main/java/com/db/vote/infra/client/FakeCpfValidationClientAdapter.java`)
antes de aceitar o voto:

| CPF enviado | Resultado |
|---|---|
| Não tem exatamente 11 dígitos numéricos | `InvalidCpfException` → `404 Not Found` |
| 11 dígitos, último dígito **par** | `ABLE_TO_VOTE` → voto aceito normalmente |
| 11 dígitos, último dígito **ímpar** | `UNABLE_TO_VOTE` → `UnableToVoteException` → `422 Unprocessable Entity` |

Essa implementação é determinística pela paridade do último dígito (não
sorteada a cada chamada), então dá para reproduzir os dois cenários de
forma confiável ao testar:

- `11122233344` (termina em `4`, par) → `ABLE_TO_VOTE`
- `11122233345` (termina em `5`, ímpar) → `UNABLE_TO_VOTE`
- `123` ou `1112223334A` → CPF inválido (formato)

## 5. Cenários de erro para testar

Todo erro retorna o mesmo formato de corpo
(`ErrorResponse` — `src/main/java/com/db/vote/api/dto/response/ErrorResponse.java`):

```json
{ "message": "descrição do erro" }
```

Mapeamento completo em
`src/main/java/com/db/vote/api/exception/GlobalExceptionHandler.java`:

| Cenário | Como provocar no Swagger UI | Status |
|---|---|---|
| Pauta inválida | `POST /api/v1/agendas` com `"title": ""` (ou ausente) | `422` |
| Pauta inexistente | `POST .../sessions`, `.../votes` ou `GET .../results` com um `agendaId` que não existe | `404` |
| Sessão já aberta | `POST /api/v1/agendas/{agendaId}/sessions` duas vezes na mesma pauta | `409` |
| Sessão fechada/inexistente | `POST .../votes` numa pauta sem sessão aberta, ou depois que `closesAt` passou | `422` |
| CPF inválido | `POST .../votes` com `cpf` fora do formato de 11 dígitos | `404` |
| Associado não habilitado a votar | `POST .../votes` com um `cpf` cujo último dígito é ímpar (ver seção 4) | `422` |
| Voto duplicado | `POST .../votes` repetindo `memberId` ou `cpf` já usados naquela pauta | `409` |

## 6. Testando fora do Swagger UI

A mesma especificação exposta em `/v3/api-docs` pode ser importada em
outras ferramentas (Postman, Insomnia: *Import → Link* apontando para
`http://localhost:8080/v3/api-docs`). Para conferência rápida via
terminal, os mesmos passos da seção 3 em `curl`:

```bash
# 3.1 Cadastrar pauta
curl -X POST http://localhost:8080/api/v1/agendas \
  -H "Content-Type: application/json" \
  -d '{"title":"Aprovação do balanço 2026","description":"Votação sobre o balanço patrimonial"}'

# 3.2 Abrir sessão (agendaId=1)
curl -X POST http://localhost:8080/api/v1/agendas/1/sessions \
  -H "Content-Type: application/json" \
  -d '{"durationMinutes":5}'

# 3.3 Votar
curl -X POST http://localhost:8080/api/v1/agendas/1/votes \
  -H "Content-Type: application/json" \
  -d '{"memberId":1,"cpf":"11122233344","voteAnswer":"YES"}'

# 3.4 Resultado
curl http://localhost:8080/api/v1/agendas/1/results
```

## 7. Troubleshooting

- **Swagger UI não carrega / 404 em `/swagger-ui.html`:** confirme que
  a aplicação subiu de verdade (procure "Started VoteApplication" nos
  logs — ver [`docs/INICIALIZACAO.md`](./INICIALIZACAO.md)) e que a
  porta `8080` está livre.
- **`/v3/api-docs` quebrou depois de mexer em dependências:** o
  projeto usa springdoc-openapi 2.8.6, que ainda não tem release
  certificado para Spring Boot 4.1.1 / Spring Framework 7 no momento
  desta doc. Existe um smoke test dedicado a isso —
  `src/test/java/com/db/vote/OpenApiDocsTest.java` — rode
  `./mvnw test -Dtest=OpenApiDocsTest` para validar isoladamente que o
  documento OpenAPI ainda é gerado corretamente.
- **Voto sempre retorna `422` mesmo com a sessão recém-aberta:**
  confira se `durationMinutes` não foi enviado como `0` ou negativo.
- **CPF sempre cai em `UNABLE_TO_VOTE` ou `404`:** revise o formato (11
  dígitos numéricos) e a paridade do último dígito, conforme a seção 4.

## Referências

- [`docs/INICIALIZACAO.md`](./INICIALIZACAO.md) — como subir a aplicação
- [`docs/01-documento-requisitos.md`](./01-documento-requisitos.md)
- `pom.xml:94-98` — dependência `springdoc-openapi-starter-webmvc-ui`
- `src/main/java/com/db/vote/api/exception/GlobalExceptionHandler.java` — mapeamento de exceções para status HTTP
- `src/main/java/com/db/vote/infra/client/FakeCpfValidationClientAdapter.java` — regra do CPF fake (Tarefa Bônus 1)
- `src/test/java/com/db/vote/OpenApiDocsTest.java` — smoke test do `/v3/api-docs`
