import { useEffect } from "react";
import { Form, Link, data, useNavigation, useRevalidator } from "react-router";
import type { Route } from "./+types/agenda-detail";
import { Alert } from "~/components/Alert";
import { Button } from "~/components/Button";
import { Container } from "~/components/Container";
import { Countdown } from "~/components/Countdown";
import { TextField } from "~/components/Field";
import { Hero } from "~/components/Hero";
import { ChevronLeftIcon } from "~/components/Icons";
import { ResultPanel } from "~/components/ResultPanel";
import { StatusBadge } from "~/components/StatusBadge";
import { api, ApiError, type Agenda, type VoteOption } from "~/lib/api";
import { displayStatus } from "~/lib/agenda";

const PARTIAL_RESULT_REFRESH_MS = 10_000;

type ActionResult = { error?: string; success?: string; submittedAt?: number };

export function meta({ loaderData }: Route.MetaArgs) {
  return [{ title: `${loaderData?.agenda.title ?? "Pauta"} · Votação Cooperativa` }];
}

export async function clientLoader({ params }: Route.ClientLoaderArgs) {
  const id = Number(params.agendaId);
  try {
    return { agenda: await api.getAgenda(id) };
  } catch (error) {
    if (error instanceof ApiError && error.status === 404) {
      throw data("Pauta não encontrada.", { status: 404 });
    }
    throw error;
  }
}

export async function clientAction({ request, params }: Route.ClientActionArgs): Promise<ActionResult> {
  const agendaId = Number(params.agendaId);
  const form = await request.formData();

  try {
    if (form.get("intent") === "open-session") {
      const minutes = Number(form.get("durationMinutes"));
      await api.openSession(agendaId, minutes > 0 ? minutes : undefined);
      return { success: "Sessão de votação aberta." };
    }

    const memberId = Number(form.get("memberId"));
    const cpf = String(form.get("cpf") ?? "").replace(/\D/g, "");
    const voteAnswer = form.get("voteAnswer") as VoteOption;
    await api.vote(agendaId, memberId, cpf, voteAnswer);
    return { success: `Voto "${voteAnswer === "YES" ? "Sim" : "Não"}" registrado com sucesso.`, submittedAt: Date.now() };
  } catch (error) {
    return { error: error instanceof ApiError ? error.message : "Erro inesperado. Tente novamente." };
  }
}

export default function AgendaDetailPage({ loaderData, actionData }: Route.ComponentProps) {
  const { agenda } = loaderData;
  const revalidator = useRevalidator();
  const isOpen = agenda.sessionStatus === "OPEN";

  // Keep the partial tally fresh while the session is open (other members voting).
  useEffect(() => {
    if (!isOpen) return;
    const timer = setInterval(() => revalidator.revalidate(), PARTIAL_RESULT_REFRESH_MS);
    return () => clearInterval(timer);
  }, [isOpen, revalidator]);

  return (
    <>
      <Hero
        eyebrow={
          <div className="mb-2 flex flex-wrap items-center gap-3">
            <Link to="/" className="flex items-center gap-1 text-[15px] font-semibold text-white no-underline hover:underline">
              <ChevronLeftIcon /> Todas as pautas
            </Link>
            <span className="rounded-full bg-white">
              <StatusBadge status={displayStatus(agenda)} />
            </span>
          </div>
        }
        title={agenda.title}
        subtitle={<SessionSubtitle agenda={agenda} onExpire={() => revalidator.revalidate()} />}
      />

      <Container className="grid grid-cols-1 items-start gap-8 pt-10 pb-16 md:pt-14 md:pb-22 lg:grid-cols-[minmax(0,1fr)_380px]">
        <div className="flex flex-col gap-8">
          {agenda.description && (
            <section className="flex flex-col gap-2">
              <h2 className="text-xl font-bold text-brand-dark">Sobre a pauta</h2>
              <p className="text-base leading-relaxed whitespace-pre-line text-ink">{agenda.description}</p>
            </section>
          )}

          {actionData?.error && <Alert tone="error">{actionData.error}</Alert>}
          {actionData?.success && <Alert tone="success">{actionData.success}</Alert>}

          {agenda.sessionStatus === "NOT_STARTED" && <OpenSessionForm />}
          {isOpen && <VoteForm key={actionData?.submittedAt} />}
          {agenda.sessionStatus === "CLOSED" && (
            <p className="rounded-lg bg-chip p-5 text-base text-ink">
              A sessão foi encerrada e não aceita novos votos. O resultado ao lado é final.
            </p>
          )}
        </div>

        <ResultPanel agenda={agenda} />
      </Container>
    </>
  );
}

function SessionSubtitle({ agenda, onExpire }: { agenda: Agenda; onExpire: () => void }) {
  if (agenda.sessionStatus === "OPEN") {
    return (
      <>
        Sessão aberta · encerra em <Countdown seconds={agenda.secondsRemaining} onExpire={onExpire} />
      </>
    );
  }
  return agenda.sessionStatus === "NOT_STARTED" ? "Aguardando abertura da sessão de votação." : "Sessão de votação encerrada.";
}

function Panel({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="flex flex-col gap-5 rounded-lg bg-white p-6 shadow-[0_2px_6px_rgba(0,0,0,0.15)]">
      <h2 className="text-xl font-bold text-brand-dark">{title}</h2>
      {children}
    </section>
  );
}

function OpenSessionForm() {
  const isSubmitting = useNavigation().state === "submitting";
  return (
    <Panel title="Abrir sessão de votação">
      <Form method="post" className="flex flex-col gap-5 sm:flex-row sm:items-end">
        <input type="hidden" name="intent" value="open-session" />
        <div className="sm:w-56">
          <TextField label="Duração (minutos)" name="durationMinutes" type="number" min={1} placeholder="1" hint="Padrão: 1 minuto." />
        </div>
        <Button type="submit" disabled={isSubmitting} className="sm:mb-[26px]">
          {isSubmitting ? "Abrindo…" : "Abrir sessão"}
        </Button>
      </Form>
    </Panel>
  );
}

function VoteForm() {
  const isSubmitting = useNavigation().state === "submitting";
  return (
    <Panel title="Registrar voto">
      <Form method="post" className="flex flex-col gap-5">
        <input type="hidden" name="intent" value="vote" />
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
          <TextField label="ID do associado" name="memberId" type="number" min={1} required placeholder="Ex.: 42" />
          <TextField
            label="CPF"
            name="cpf"
            required
            inputMode="numeric"
            pattern="\d{11}"
            maxLength={11}
            placeholder="Somente números"
            title="Informe os 11 dígitos do CPF, sem pontuação"
            hint="Ambiente de teste: CPFs terminados em dígito par estão aptos a votar."
          />
        </div>
        <fieldset className="flex flex-col gap-2" disabled={isSubmitting}>
          <legend className="mb-2 text-[15px] font-semibold text-ink">Seu voto</legend>
          <div className="grid grid-cols-2 gap-3">
            <Button type="submit" name="voteAnswer" value="YES" className="w-full">
              Sim
            </Button>
            <Button type="submit" name="voteAnswer" value="NO" variant="outline" className="w-full">
              Não
            </Button>
          </div>
        </fieldset>
        <p className="text-sm text-muted">Cada associado vota uma única vez por pauta.</p>
      </Form>
    </Panel>
  );
}
