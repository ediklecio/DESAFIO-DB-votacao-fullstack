import type { ReactNode } from "react";

type Tone = "error" | "success";

const TONES: Record<Tone, string> = {
  error: "border-danger/30 bg-danger-soft text-danger",
  success: "border-success/30 bg-success-soft text-success",
};

export function Alert({ tone, children }: { tone: Tone; children: ReactNode }) {
  return (
    <div role={tone === "error" ? "alert" : "status"} className={`rounded-md border px-4 py-3 text-[15px] font-semibold ${TONES[tone]}`}>
      {children}
    </div>
  );
}
