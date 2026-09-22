import { useRevalidator, useSearchParams } from "react-router";
import type { Route } from "./+types/agendas";
import { AgendaCard } from "~/components/AgendaCard";
import { ButtonLink } from "~/components/Button";
import { Container } from "~/components/Container";
import { FilterTabs, type FilterTab } from "~/components/FilterTabs";
import { Hero } from "~/components/Hero";
import { PlusIcon } from "~/components/Icons";
import { api, type Agenda } from "~/lib/api";
import { displayStatus, pluralize } from "~/lib/agenda";

type Filter = "todas" | "votacao" | "aguardando" | "encerradas";

const FILTERS: { id: Filter; label: string }[] = [
  { id: "todas", label: "Todas" },
  { id: "votacao", label: "Em votação" },
  { id: "aguardando", label: "Aguardando" },
  { id: "encerradas", label: "Encerradas" },
];

function matches(agenda: Agenda, filter: Filter) {
  const status = displayStatus(agenda);
  if (filter === "todas") return true;
  if (filter === "encerradas") return status === "aprovada" || status === "rejeitada" || status === "empate";
  return status === filter;
}

function parseFilter(value: string | null): Filter {
  return FILTERS.some((f) => f.id === value) ? (value as Filter) : "todas";
}

export function meta() {
  return [{ title: "Pautas · Votação Cooperativa" }];
}

export async function clientLoader() {
  const page = await api.listAgendas();
  return { agendas: page.content, total: page.totalElements };
}

export default function AgendasPage({ loaderData }: Route.ComponentProps) {
  const { agendas, total } = loaderData;
  const [searchParams, setSearchParams] = useSearchParams();
  const revalidator = useRevalidator();
  const filter = parseFilter(searchParams.get("filtro"));

  const tabs: FilterTab<Filter>[] = FILTERS.map((f) => ({ ...f, count: agendas.filter((a) => matches(a, f.id)).length }));
  const visible = agendas.filter((a) => matches(a, filter));
  const openCount = agendas.filter((a) => a.sessionStatus === "OPEN").length;

  const changeFilter = (id: Filter) => setSearchParams(id === "todas" ? {} : { filtro: id }, { preventScrollReset: true });
  const refresh = () => revalidator.revalidate();

  return (
    <>
      <Hero
        title={filter === "encerradas" ? "Resultados" : "Pautas"}
        subtitle={filter === "encerradas" ? "Confira a apuração das pautas encerradas." : "Acompanhe as assembleias e registre seu voto."}
        action={
          <ButtonLink to="/pautas/nova">
            <PlusIcon />
            Nova pauta
          </ButtonLink>
        }
      />

      <Container className="flex flex-col gap-7 pt-10 pb-16 md:pt-14 md:pb-22">
        <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <FilterTabs tabs={tabs} active={filter} onChange={changeFilter} />
          <span className="text-[15px] text-muted">
            {pluralize(total, "pauta", "pautas")} · {openCount} em votação agora
          </span>
        </div>

        {visible.length > 0 ? (
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3">
            {visible.map((agenda) => (
              <AgendaCard key={agenda.id} agenda={agenda} onSessionExpire={refresh} />
            ))}
          </div>
        ) : (
          <EmptyState hasAgendas={agendas.length > 0} />
        )}
      </Container>
    </>
  );
}

function EmptyState({ hasAgendas }: { hasAgendas: boolean }) {
  return (
    <div className="flex flex-col items-start gap-4 rounded-lg border border-dashed border-line p-8">
      <p className="text-lg font-semibold text-ink">
        {hasAgendas ? "Nenhuma pauta neste filtro." : "Nenhuma pauta cadastrada ainda."}
      </p>
      {!hasAgendas && (
        <ButtonLink to="/pautas/nova" variant="outline">
          <PlusIcon />
          Cadastrar a primeira pauta
        </ButtonLink>
      )}
    </div>
  );
}
