# Sistema de Votação Cooperativista

Solução full stack para gerenciar pautas e sessões de votação em assembleias de cooperativa: cadastro de pauta, abertura de sessão de votação com duração configurável, registro de voto (Sim/Não) por associado e apuração do resultado.

- **Backend** (`backend/`): API REST em Java 25 / Spring Boot 4, PostgreSQL + Flyway.
- **Front-end** (`frontend/`): React 19 + React Router (modo SPA) + Tailwind, servido por nginx.

## Início rápido (Docker)

Pré-requisitos: Docker e Docker Compose instalados.

```bash
cp .env.example .env      # defina DB_PASSWORD; não altere DB_HOST
docker compose up --build
```

| Serviço | URL |
|---|---|
| Front-end | http://localhost:3000 |
| API | http://localhost:8080/api/v1/agendas |
| Swagger UI | http://localhost:8080/swagger-ui.html |

Para usar outra porta no front, defina `WEB_PORT` no `.env` (ex.: `WEB_PORT=8081`).

## Usando o front-end

1. **Nova pauta** — no menu ou no botão "Nova pauta", informe título (obrigatório) e descrição. Ao salvar, você cai na página da pauta.
2. **Abrir sessão** — na pauta com status *Aguardando sessão*, informe a duração em minutos (vazio = 1 minuto) e clique em **Abrir sessão**.
3. **Votar** — com a sessão *Em votação*, informe o ID do associado e o CPF (11 dígitos, só números) e clique em **Sim** ou **Não**.
   - Ambiente de teste: o validador fake aceita CPFs terminados em dígito **par** (ex.: `12345678902`); final ímpar é recusado como "não apto".
   - Cada associado (e cada CPF) vota uma única vez por pauta.
4. **Resultado** — o painel "Resultado" mostra a contagem **Parcial** durante a sessão (atualiza a cada 10 s) e **Final** após o encerramento. A lista **Resultados** (menu) filtra as pautas encerradas: *Aprovada* (mais Sim), *Rejeitada* (mais Não) ou *Empate*.

A tela inicial lista as pautas com filtros por status e contagem regressiva das sessões abertas.

### Rodando o front-end localmente (desenvolvimento)

Com a API no ar em `localhost:8080` (via Docker ou IDE):

```bash
cd frontend
npm install
npm run dev               # http://localhost:5173 — /api é redirecionado para localhost:8080
```

Se a API estiver em outro endereço: `API_URL=http://host:porta npm run dev`.

## Documentação

A documentação do backend — requisitos, arquitetura, guias de execução e de teste — está em [`backend/docs`](backend/docs).

| Documento | Descrição |
|---|---|
| [`DESAFIO.md`](backend/docs/DESAFIO.md) | Enunciado original do desafio: requisitos, tarefas bônus e critérios de avaliação |
| [`01-documento-requisitos.md`](backend/docs/01-documento-requisitos.md) | Requisitos funcionais, regras de negócio e requisitos não funcionais (RF/RN/RNF) |
| [`02-diagrama-classes.md`](backend/docs/02-diagrama-classes.md) | Diagrama de classes e organização em camadas (`api` / `domain` / `repository` / `service` / `infra`) |
| [`03-guia-testes-swagger.md`](backend/docs/03-guia-testes-swagger.md) | Passo a passo para testar a API pelo Swagger UI, incluindo cenários de erro |
| [`INICIALIZACAO.md`](backend/docs/INICIALIZACAO.md) | Guia de inicialização: Docker Compose, ambiente híbrido (dev local) e testes automatizados |
| [`DESIGN_PATTERN.md`](docs/DESIGN_PATTERN.md) | Referência visual (paleta, tipografia) usada no front-end |
