import type { DisplayStatus } from "~/lib/agenda";

const BADGES: Record<DisplayStatus, { label: string; className: string; dot?: boolean }> = {
  votacao: { label: "Em votação", className: "bg-brand-soft text-brand-700", dot: true },
  aguardando: { label: "Aguardando sessão", className: "bg-chip text-ink" },
  aprovada: { label: "Encerrada · Aprovada", className: "bg-success-soft text-success" },
  rejeitada: { label: "Encerrada · Rejeitada", className: "bg-danger-soft text-danger" },
  empate: { label: "Encerrada · Empate", className: "bg-chip text-ink" },
};

export function StatusBadge({ status }: { status: DisplayStatus }) {
  const badge = BADGES[status];
  return (
    <span className={`inline-flex h-[26px] items-center gap-1.5 self-start rounded-full px-2.5 text-[13px] font-bold ${badge.className}`}>
      {badge.dot && <span className="size-2 rounded-full bg-brand" />}
      {badge.label}
    </span>
  );
}
