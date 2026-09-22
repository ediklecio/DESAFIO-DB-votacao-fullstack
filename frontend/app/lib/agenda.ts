import type { Agenda } from "./api";

export type DisplayStatus = "votacao" | "aguardando" | "aprovada" | "rejeitada" | "empate";

/** Maps the API session status + tally to what the UI shows (approved = more YES than NO). */
export function displayStatus(agenda: Agenda): DisplayStatus {
  if (agenda.sessionStatus === "OPEN") return "votacao";
  if (agenda.sessionStatus === "NOT_STARTED") return "aguardando";
  if (agenda.totalYes > agenda.totalNo) return "aprovada";
  if (agenda.totalYes < agenda.totalNo) return "rejeitada";
  return "empate";
}

export function totalVotes(agenda: Agenda) {
  return agenda.totalYes + agenda.totalNo;
}

export function formatCountdown(seconds: number) {
  const safe = Math.max(0, seconds);
  const h = Math.floor(safe / 3600);
  const m = Math.floor((safe % 3600) / 60);
  const s = safe % 60;
  const mmss = `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
  return h > 0 ? `${h}:${mmss}` : mmss;
}

export function pluralize(count: number, singular: string, plural: string) {
  return `${count} ${count === 1 ? singular : plural}`;
}
