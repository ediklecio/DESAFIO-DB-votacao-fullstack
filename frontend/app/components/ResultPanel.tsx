import type { Agenda } from "~/lib/api";
import { pluralize, totalVotes } from "~/lib/agenda";

function percent(part: number, total: number) {
  return total === 0 ? 0 : Math.round((part / total) * 100);
}

function ResultRow({ label, count, total, barClass }: { label: string; count: number; total: number; barClass: string }) {
  const pct = percent(count, total);
  return (
    <div className="flex flex-col gap-1.5">
      <div className="flex items-baseline justify-between">
        <span className="text-base font-semibold text-ink">{label}</span>
        <span className="text-sm text-muted">
          <strong className="text-lg text-ink tabular-nums">{count}</strong> · {pct}%
        </span>
      </div>
      <div className="h-3 overflow-hidden rounded-full bg-chip" role="presentation">
        <div className={`h-full rounded-full ${barClass}`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  );
}

export function ResultPanel({ agenda }: { agenda: Agenda }) {
  const total = totalVotes(agenda);
  const isFinal = agenda.sessionStatus === "CLOSED";

  return (
    <section aria-labelledby="result-title" className="flex flex-col gap-5 rounded-lg bg-white p-6 shadow-[0_2px_6px_rgba(0,0,0,0.15)]">
      <div className="flex items-center justify-between gap-3">
        <h2 id="result-title" className="text-xl font-bold text-brand-dark">
          Resultado
        </h2>
        <span className={`rounded-full px-2.5 py-1 text-[13px] font-bold ${isFinal ? "bg-brand-dark text-white" : "bg-chip text-ink"}`}>
          {isFinal ? "Final" : "Parcial"}
        </span>
      </div>
      <ResultRow label="Sim" count={agenda.totalYes} total={total} barClass="bg-brand" />
      <ResultRow label="Não" count={agenda.totalNo} total={total} barClass="bg-danger" />
      <p className="border-t border-line-soft pt-4 text-sm text-muted">
        {pluralize(total, "voto registrado", "votos registrados")}
        {agenda.sessionStatus === "NOT_STARTED" && " · a sessão ainda não foi aberta"}
      </p>
    </section>
  );
}
