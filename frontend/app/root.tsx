import type { ReactNode } from "react";
import { isRouteErrorResponse, Links, Meta, Outlet, Scripts, ScrollRestoration } from "react-router";

import type { Route } from "./+types/root";
import "./app.css";
import { ButtonLink } from "./components/Button";
import { Container } from "./components/Container";
import { Footer } from "./components/Footer";
import { Header } from "./components/Header";
import { Hero } from "./components/Hero";

export const links: Route.LinksFunction = () => [
  { rel: "preconnect", href: "https://fonts.googleapis.com" },
  { rel: "preconnect", href: "https://fonts.gstatic.com", crossOrigin: "anonymous" },
  { rel: "stylesheet", href: "https://fonts.googleapis.com/css2?family=Asap:wght@400;500;600;700&display=swap" },
];

export function Layout({ children }: { children: ReactNode }) {
  return (
    <html lang="pt-BR">
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <Meta />
        <Links />
      </head>
      <body>
        {children}
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  );
}

function Shell({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">{children}</main>
      <Footer />
    </div>
  );
}

export default function App() {
  return (
    <Shell>
      <Outlet />
    </Shell>
  );
}

export function HydrateFallback() {
  return (
    <div className="flex min-h-screen flex-col">
      <div className="h-[72px] bg-brand-dark" />
      <p className="p-10 text-center text-muted">Carregando…</p>
    </div>
  );
}

export function ErrorBoundary({ error }: Route.ErrorBoundaryProps) {
  let title = "Algo deu errado";
  let details = error instanceof Error ? error.message : "Ocorreu um erro inesperado.";

  if (isRouteErrorResponse(error)) {
    title = error.status === 404 ? "Não encontrado" : `Erro ${error.status}`;
    details = typeof error.data === "string" ? error.data : "A página solicitada não existe.";
  }

  return (
    <Shell>
      <Hero title={title} subtitle={details} />
      <Container className="py-14">
        <ButtonLink to="/">Voltar para as pautas</ButtonLink>
      </Container>
    </Shell>
  );
}
