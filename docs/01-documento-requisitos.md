# Documento de Requisitos — Sistema de Votação em Assembleias de Cooperativa

**Projeto:** desafio-votacao-fullstack
**Status:** MVP em desenvolvimento
**Referência:** `README.md` (enunciado original)

## 1. Objetivo e contexto

No cooperativismo, cada associado possui um voto e as decisões da cooperativa são tomadas em assembleia, por votação. Este documento formaliza os requisitos de uma solução web (API REST + front-end) para gerenciar pautas, abrir sessões de votação com prazo determinado, receber votos dos associados e apurar o resultado — de forma que pautas e votos sobrevivam a um restart da aplicação.

A segurança das interfaces está fora do escopo deste exercício: toda chamada às APIs é considerada autorizada, conforme o próprio enunciado do desafio.

## 2. Atores

| Ator | Papel |
|---|---|
| Associado | Vota em uma pauta, identificado por um id único; pode votar apenas uma vez por pauta |
| Sistema (backend) | Cadastra pautas, controla a janela de tempo da sessão, valida unicidade do voto e apura o resultado |
| Client externo de CPF (bônus) | Serviço fake que informa se um CPF é apto a votar |

## 3. Escopo

### 3.1 Escopo do MVP (obrigatório)

- Cadastro de pauta
- Abertura de sessão de votação em uma pauta, com duração configurável (default de 1 minuto quando não informada)
- Registro de voto (Sim/Não) de um associado em uma pauta, um voto por associado por pauta
- Apuração e consulta do resultado da votação
- Persistência de pautas e votos em banco de dados, sem perda de dados em restart
- Execução via Docker, com docker-compose orquestrando aplicação, rede e banco de dados
- Configuração de conexão com o banco via arquivo `.env`, com o banco rodando como serviço externo ao container da aplicação

### 3.2 Tarefas bônus (opcionais, avaliadas à parte)

- Integração com client fake de validação de CPF (`ABLE_TO_VOTE` / `UNABLE_TO_VOTE`, 404 para CPF inválido)
- Comportamento performático em cenários de centenas de milhares de votos, com testes de performance
- Estratégia de versionamento de API

### 3.3 Fora de escopo

Por decisão de simplicidade (evitar over engineering, conforme os próprios critérios de avaliação do desafio), ficam fora do escopo deste MVP: cadastro/gestão de associados (assume-se que o id do associado já existe e é informado pelo cliente da API), autenticação e autorização, suporte a múltiplas cooperativas (multi-tenancy), voto secreto, quórum por convocação, delegação de voto e participação remota via vídeo. Essas ideias foram exploradas como possível "V2" mas não fazem parte do enunciado e não serão implementadas nesta entrega.

## 4. Requisitos funcionais

| ID | Requisito | Descrição |
|---|---|---|
| RF01 | Cadastrar pauta | Permitir criar uma pauta com um identificador e uma descrição/título, para posterior abertura de sessão |
| RF02 | Abrir sessão de votação | Abrir uma sessão de votação vinculada a uma pauta, com duração em minutos informada na chamada; se omitida, usar 1 minuto por padrão |
| RF03 | Registrar voto | Receber o voto (`SIM`/`NAO`) de um associado identificado por id único, para uma pauta com sessão aberta |
| RF04 | Impedir voto duplicado | Rejeitar o voto de um associado que já votou naquela pauta |
| RF05 | Apurar resultado | Contabilizar os votos `SIM` e `NAO` de uma pauta e retornar o resultado |
| RF06 (bônus) | Validar CPF | Antes de aceitar o voto, consultar um client fake que informa se o CPF é apto a votar; se o CPF for inválido, retornar 404; se `UNABLE_TO_VOTE`, rejeitar o voto |
| RF07 (bônus) | Versionar API | Expor os endpoints sob um prefixo de versão (`/api/v1/...`) |
| RF08 | Containerizar e orquestrar via Docker | Disponibilizar `Dockerfile` da aplicação e um `docker-compose.yml` que orquestre aplicação, rede dedicada e banco de dados, seguindo boas práticas |

## 5. Regras de negócio

| ID | Regra |
|---|---|
| RN01 | Um associado vota no máximo uma vez por pauta — unicidade garantida por constraint `UNIQUE` no banco (`pauta_id` + `associado_id`), não apenas em memória |
| RN02 | Um voto só é aceito enquanto a sessão da pauta estiver aberta, isto é, dentro da janela `[abertura, abertura + duração)` |
| RN03 | Se a duração da sessão não for informada na abertura, o padrão é 1 minuto |
| RN04 | Uma pauta sem sessão aberta (ainda não iniciada ou já encerrada) não aceita novos votos |
| RN05 | O status "aberta/encerrada" da sessão é derivado por comparação de data/hora no momento da consulta ou do voto, sem depender de um job/scheduler |
| RN06 | O resultado pode ser consultado a qualquer momento; a resposta deve deixar claro se a sessão já foi encerrada ou se a contagem é parcial |
| RN07 (bônus) | Se a validação externa de CPF retornar CPF inválido (404) ou `UNABLE_TO_VOTE`, o voto é rejeitado e a API responde com um erro apropriado (não um voto silenciosamente descartado) |
| RN08 | O banco de dados roda como serviço próprio no `docker-compose`, externo ao container da aplicação; a aplicação se conecta a ele via variáveis de ambiente carregadas de um arquivo `.env` (host, porta, usuário, senha, nome do banco) |

## 6. Requisitos não funcionais

| ID | Requisito |
|---|---|
| RNF01 | Persistência: pautas, sessões e votos devem sobreviver a um restart da aplicação — PostgreSQL como banco de dados, executado como serviço próprio via `docker-compose`; H2 em memória permanece apenas para testes automatizados |
| RNF02 | Performance: comportamento estável com centenas de milhares de votos — índice/constraint eficientes, sem N+1, validado por testes de performance (bônus 2) |
| RNF03 | Arquitetura: monólito modular, sem filas ou cache adicionais nesta fase — decisão de simplicidade para evitar complexidade desnecessária, alinhada aos critérios de avaliação do desafio |
| RNF04 | Versionamento: estratégia de versionamento de API definida e documentada (path-based, `/api/v1`) |
| RNF05 | Qualidade de código: SOLID, Clean Code, testes unitários e de integração, tratamento de exceções centralizado, logging nos pontos críticos |
| RNF06 | Documentação: OpenAPI/Swagger para a API e README com instruções completas de execução, incluindo eventuais dependências externas |
| RNF07 | Front-end: layout responsivo |
| RNF08 | Segurança: abstraída para fins do exercício — toda chamada é considerada autorizada |
| RNF09 | Orquestração: `docker-compose` com rede dedicada, volume nomeado para persistência dos dados do banco, healthcheck no serviço de banco e `depends_on` com `condition: service_healthy` na aplicação |
| RNF10 | Configuração de ambiente: dados de conexão com o banco (host, porta, usuário, senha, nome do banco) centralizados em `.env`, nunca hardcoded no código ou no `docker-compose.yml`; um `.env.example` deve documentar as variáveis esperadas sem expor segredos reais |

## 7. Modelo de domínio (visão de requisitos)

- **Pauta**: identificador, título/descrição
- **SessaoVotacao**: identificador, referência à pauta, data/hora de abertura, duração (ou data/hora de encerramento calculada)
- **Voto**: identificador, referência à pauta, id do associado, opção (`SIM`/`NAO`), data/hora do registro — constraint `UNIQUE` composta (pauta + associado)

Status atual da implementação: a entidade `Vote` e o enum `VoteOption` já existem, com constraint de unicidade `agenda_id + member_id` e `VoteService`/`VoteRepository` básicos. As entidades `Pauta` e `SessaoVotacao` (RF01 e RF02) ainda não foram criadas — é o próximo passo da Fase 1 do plano de execução.

## 8. Critérios de aceite

Alinhados aos critérios de avaliação declarados no enunciado do desafio: simplicidade da solução (sem over engineering), organização e arquitetura do código, boas práticas (manutenibilidade, legibilidade), tratamento de erros e exceções, testes automatizados, limpeza de código, documentação do código e da API, logs da aplicação, organização dos commits e layout responsivo do front-end.

## 9. Referências

- `Desafio.md` — enunciado original do desafio
- `AGENTE_JAVA_SR_PROMPT.md` — padrões técnicos obrigatórios (stack, arquitetura, SOLID, Clean Code)
- `SOLID-Guia-Completo.md` — guia de princípios SOLID aplicados ao projeto
