# Inicialização
Indice de ambientes para inicialização da aplicação.

| Perfil | Quando usar | Banco | Comando principal |
|---|---|---|---|
| [1. Docker Compose](#1-docker-compose-stack-completa) | Rodar a aplicação completa como será avaliada | PostgreSQL (container) | `docker compose up --build` |
| [2. Híbrido (dev local)](#2-híbrido-app-local--banco-em-docker) | Desenvolver com hot-reload/debug na IDE | PostgreSQL (container) | `./mvnw spring-boot:run` |
| [3. Testes automatizados](#3-testes-automatizados) | Rodar a suíte de testes | H2 (em memória) | `./mvnw test` |

---

## 1. Docker Compose (stack completa)

Perfil padrão de execução. Sobe API + PostgreSQL na rede `vote-network`
(`docker-compose.yml:36-38`), com o Postgres persistido no volume nomeado
`vote-db-data` (`docker-compose.yml:9-10` — ver detalhes de persistência
mais abaixo).

**Pré-requisitos:** Docker e Docker Compose instalados.

**Passos:**

1. Copie o arquivo de exemplo de variáveis de ambiente:
   ```bash
   cp .env.example .env
   ```
2. Abra `.env` e defina uma senha real em `DB_PASSWORD` (o valor de exemplo
   é `change-me`). **Não altere `DB_HOST`** — ele precisa continuar `db`,
   o nome do serviço no compose, para a API alcançar o Postgres pela rede
   interna (`.env.example:3-5`).
3. Suba os containers:
   ```bash
   docker compose up --build
   ```
   O `app` só inicia depois que o `healthcheck` do `db` reportar saudável
   (`docker-compose.yml:30-32`, condição `service_healthy`), então é normal
   ver só os logs do Postgres nos primeiros segundos.
4. Aguarde a linha de log do Spring Boot indicando que a aplicação subiu
   (porta `8080`, mapeada em `docker-compose.yml:28-29`).
5. Acesse:
   - Front-end: http://localhost:3000 (serviço `web`; porta configurável via `WEB_PORT` no `.env`)
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - OpenAPI JSON: http://localhost:8080/v3/api-docs
   - API: http://localhost:8080/api/v1/agendas

**Para parar:**
```bash
docker compose down       # para os containers, mantém os dados do volume
docker compose down -v    # para os containers E apaga o volume (perde os dados)
```

**Persistência:** o Flyway roda automaticamente no startup do `app` e cria o
schema no Postgres (`spring.flyway.enabled=true`,
`application.properties:10`). Os dados sobrevivem a `docker compose down`,
restarts e rebuilds da imagem — só são apagados com `down -v` ou
`docker volume rm vote-db-data`.

---

## 2. Híbrido (app local + banco em Docker)

Útil para debugar/desenvolver na IDE com hot-reload, mas ainda usando um
Postgres real (não H2) para se aproximar do comportamento de produção,
incluindo as migrations do Flyway.

**Pré-requisitos:** Docker, JDK 25, Maven (ou use o wrapper `./mvnw`
incluso no repo).

**Passos:**

1. Suba **só** o banco:
   ```bash
   docker compose up -d db
   ```
2. Exporte as variáveis de ambiente apontando para o Postgres exposto pelo
   Docker, mas agora com `DB_HOST=localhost` (fora da rede do compose, o
   nome de serviço `db` não resolve):
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=vote
   export DB_USER=vote
   export DB_PASSWORD=<mesma senha que você colocou no .env>
   ```
   > No PowerShell: `$env:DB_HOST = "localhost"` (e assim por diante para
   > as demais variáveis).

   Se preferir, configure essas mesmas variáveis na *Run Configuration* da
   sua IDE em vez de exportá-las no shell.
3. Rode a aplicação:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Acesse os mesmos endpoints do perfil 1 (`localhost:8080/swagger-ui.html`).

**Para parar:** `Ctrl+C` na aplicação e, se quiser, `docker compose stop db`.

---

## 3. Testes automatizados

Roda inteiramente contra H2 em memória — não depende de Docker nem de
Postgres. `src/test/resources/application.properties` sobrescreve a config
principal: Flyway desabilitado e `ddl-auto=create-drop`, ou seja, o
Hibernate cria o schema direto a partir das entidades
(`src/test/resources/application.properties:7-8`).

**Pré-requisitos:** JDK 25 (nenhum banco externo).

**Passos:**

```bash
./mvnw test
```

Isso inclui os testes unitários de domínio/serviço, o smoke test do
Swagger (`OpenApiDocsTest`) e o teste de integração de fluxo completo
(`VotingFlowIntegrationTest`, pauta → sessão → voto → resultado).

---

## Variáveis de ambiente (referência)

Definidas em `.env` (perfis 1 e 2) e lidas em
`application.properties:5-7`:

| Variável | Descrição | Valor default se ausente |
|---|---|---|
| `DB_HOST` | Host do Postgres | `localhost` |
| `DB_PORT` | Porta do Postgres | `5432` |
| `DB_NAME` | Nome do banco | `vote` |
| `DB_USER` | Usuário do banco | `vote` |
| `DB_PASSWORD` | Senha do banco | `vote` |

**Nunca commite o `.env` real** — ele já está no `.gitignore`; apenas
`.env.example` é versionado.

## Troubleshooting

- **App não conecta no banco ao rodar via Docker Compose:** confira se
  `DB_HOST=db` no `.env` (não `localhost`) — dentro da rede do compose, o
  serviço é resolvido pelo nome.
- **App não conecta no banco ao rodar híbrido (perfil 2):** confira se
  `DB_HOST=localhost` nas variáveis exportadas — fora do compose, `db` não
  resolve.
- **Erro de Flyway/schema desatualizado:** ao trocar de branch com novas
  migrations, suba o container do banco do zero
  (`docker compose down -v && docker compose up -d db`) para reaplicar as
  migrations em um schema limpo.
