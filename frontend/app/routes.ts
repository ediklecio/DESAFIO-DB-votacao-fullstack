import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
  index("routes/agendas.tsx"),
  route("pautas/nova", "routes/new-agenda.tsx"),
  route("pautas/:agendaId", "routes/agenda-detail.tsx"),
] satisfies RouteConfig;
