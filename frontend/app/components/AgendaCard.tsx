import { Link } from "react-router";
import type { Agenda } from "~/lib/api";
import { displayStatus, pluralize, totalVotes } from "~/lib/agenda";
import { Countdown } from "./Countdown";
import { ChevronRightIcon } from "./Icons";
import { StatusBadge } from "./StatusBadge";

const CTA_BY_STATUS = {
  votacao: "Votar",
  aguardando: "Abrir sessão",
  aprovada: "Ver resultado",
  rejeitada: "Ver resultado",
  empate: "Ver resultado",
} as const;

type AgendaCardProps = { agenda: Agenda; onSessionExpire: () => void };

export function AgendaCard({ agenda, onSessionExpire }: AgendaCardProps) {
  const status = displayStatus(agenda);

  return (
    <Link
      to={`/pautas/${agenda.id}`}
      className="flex min-h-[232px] flex-col gap-3 rounded-lg bg-white p-6 text-ink no-underline shadow-[0_2px_6px_rgba(0,0,0,0.15)] transition-shadow hover:shadow-[0_6px_16px_rgba(0,54,65,0.18)] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent"
    >
      <StatusBadge status={status} />
      <h2 className="line-clamp-2 text-[19px] leading-snug font-semibold text-ink">{agenda.title}</h2>
      {agenda.description && <p className="line-clamp-2 text-[15px] leading-normal text-muted">{agenda.description}</p>}

      <div className="mt-auto flex items-center justify-between border-t border-line-soft pt-3.5 text-sm">
        <span className="text-muted">
          {status === "votacao" && (
            <>
              Encerra em <Countdown seconds={agenda.secondsRemaining} onExpire={onSessionExpire} />
            </>
          )}
          {status === "aguardando" && "Sessão não iniciada"}
          {(status === "aprovada" || status === "rejeitada" || status === "empate") &&
            pluralize(totalVotes(agenda), "voto", "votos")}
        </span>
        <span className="flex items-center gap-1 font-bold text-brand-dark">
          {CTA_BY_STATUS[status]}
          <ChevronRightIcon />
        </span>
      </div>
    </Link>
  );
}
