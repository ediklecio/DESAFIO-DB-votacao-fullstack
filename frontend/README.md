# Front-end — Votação Cooperativa

React 19 + React Router (framework mode, `ssr: false` → SPA) + Tailwind CSS v4.

```bash
npm install
npm run dev         # http://localhost:5173 (proxy /api -> http://localhost:8080; sobrescreva com API_URL)
npm run typecheck
npm run build       # gera build/client (estático)
```

Em Docker, o `Dockerfile` gera o bundle e o serve com nginx (`nginx.conf`), que também faz proxy de `/api` para o serviço `api` do compose — o browser fala com uma única origem, sem CORS.

## Estrutura

```
app/
├── root.tsx              # layout (header/footer), fallback e error boundary
├── routes.ts             # /  ·  /pautas/nova  ·  /pautas/:agendaId
├── routes/               # telas: lista, nova pauta, detalhe (abrir sessão, votar, resultado)
├── components/           # UI reutilizável (Hero, AgendaCard, StatusBadge, ResultPanel, Field, Button…)
├── hooks/useCountdown.ts # contagem regressiva a partir de secondsRemaining do servidor
└── lib/                  # api.ts (client REST + tipos) · agenda.ts (regras de exibição)
```

Dados são carregados com `clientLoader` e mutações feitas com `<Form>` + `clientAction`; o React Router revalida a tela automaticamente após cada ação.
