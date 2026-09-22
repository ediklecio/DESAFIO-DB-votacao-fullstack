# Sistema de Votação Cooperativista

API REST em Java/Spring Boot para gerenciar pautas e sessões de votação em assembleias de cooperativa: cadastro de pauta, abertura de sessão de votação com duração configurável, registro de voto (Sim/Não) por associado e apuração do resultado — com persistência em PostgreSQL.

## Início rápido (Docker)

Pré-requisitos: Docker e Docker Compose instalados.

```bash
cp .env.example .env      # defina DB_PASSWORD; não altere DB_HOST
docker compose up --build
```

A API sobe em `http://localhost:8080` e o Swagger UI em `http://localhost:8080/swagger-ui.html`.

Para os demais perfis de execução — desenvolvimento local com hot-reload/debug na IDE e execução da suíte de testes automatizados — veja o guia completo em [`docs/INICIALIZACAO.md`](docs/INICIALIZACAO.md).

## Documentação

Toda a documentação do projeto — requisitos, arquitetura, guias de execução e de teste — está no diretório [`/docs`](docs).

| Documento | Descrição |
|---|---|
| [`DESAFIO.md`](docs/DESAFIO.md) | Enunciado original do desafio: requisitos, tarefas bônus e critérios de avaliação |
| [`01-documento-requisitos.md`](docs/01-documento-requisitos.md) | Requisitos funcionais, regras de negócio e requisitos não funcionais (RF/RN/RNF) |
| [`02-diagrama-classes.md`](docs/02-diagrama-classes.md) | Diagrama de classes e organização em camadas (`api` / `domain` / `repository` / `service` / `infra`) |
| [`03-guia-testes-swagger.md`](docs/03-guia-testes-swagger.md) | Passo a passo para testar a API pelo Swagger UI, incluindo cenários de erro |
| [`INICIALIZACAO.md`](docs/INICIALIZACAO.md) | Guia de inicialização: Docker Compose, ambiente híbrido (dev local) e testes automatizados |
