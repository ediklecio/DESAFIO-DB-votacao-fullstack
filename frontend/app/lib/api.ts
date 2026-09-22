export type SessionStatus = "NOT_STARTED" | "OPEN" | "CLOSED";
export type VoteOption = "YES" | "NO";

export interface Agenda {
  id: number;
  title: string;
  description: string | null;
  sessionStatus: SessionStatus;
  openedAt: string | null;
  closesAt: string | null;
  secondsRemaining: number;
  totalYes: number;
  totalNo: number;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Error carrying the backend's ErrorResponse message and HTTP status. */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
  }
}

const BASE_URL = "/api/v1";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(BASE_URL + path, {
      ...init,
      headers: { "Content-Type": "application/json", ...init?.headers },
    });
  } catch {
    throw new ApiError(0, "Não foi possível conectar à API. Verifique se o backend está no ar.");
  }

  if (!response.ok) {
    const body = await response.json().catch(() => null);
    throw new ApiError(response.status, body?.message ?? `Erro ${response.status} ao chamar a API.`);
  }
  return response.json() as Promise<T>;
}

export const api = {
  listAgendas: (size = 100) => request<Page<Agenda>>(`/agendas?size=${size}`),

  getAgenda: (id: number) => request<Agenda>(`/agendas/${id}`),

  createAgenda: (title: string, description: string) =>
    request<{ id: number }>("/agendas", {
      method: "POST",
      body: JSON.stringify({ title, description: description || null }),
    }),

  openSession: (agendaId: number, durationMinutes?: number) =>
    request<unknown>(`/agendas/${agendaId}/sessions`, {
      method: "POST",
      body: JSON.stringify({ durationMinutes: durationMinutes ?? null }),
    }),

  vote: (agendaId: number, memberId: number, cpf: string, voteAnswer: VoteOption) =>
    request<unknown>(`/agendas/${agendaId}/votes`, {
      method: "POST",
      body: JSON.stringify({ memberId, cpf, voteAnswer }),
    }),
};
