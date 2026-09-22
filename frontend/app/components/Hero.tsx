import type { ReactNode } from "react";
import { Container } from "./Container";
import { TrianglesPattern } from "./Icons";

type HeroProps = {
  title: string;
  subtitle?: ReactNode;
  eyebrow?: ReactNode;
  action?: ReactNode;
};

export function Hero({ title, subtitle, eyebrow, action }: HeroProps) {
  return (
    <section className="relative overflow-hidden bg-brand-600">
      <TrianglesPattern />
      <Container className="relative flex min-h-[200px] flex-col justify-center gap-6 py-10 md:flex-row md:items-center md:justify-between">
        <div className="flex min-w-0 flex-col gap-2">
          {eyebrow}
          <h1 className="text-4xl leading-tight font-bold text-white md:text-5xl">{title}</h1>
          {subtitle && <p className="text-lg leading-snug text-white md:text-2xl">{subtitle}</p>}
        </div>
        {action && <div className="shrink-0">{action}</div>}
      </Container>
    </section>
  );
}
