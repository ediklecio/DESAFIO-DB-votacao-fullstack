import { Container } from "./Container";

export function Footer() {
  return (
    <footer className="bg-brand-dark">
      <Container className="flex flex-col gap-1 py-7 sm:flex-row sm:items-center sm:justify-between">
        <span className="text-[15px] font-bold text-white">Votação Cooperativa</span>
        <span className="text-sm text-white">Um associado, um voto por pauta.</span>
      </Container>
      <div className="h-10 overflow-hidden bg-brand-600" aria-hidden="true">
        <svg width="100%" height="40">
          <defs>
            <pattern id="footer-triangles" width="120" height="40" patternUnits="userSpaceOnUse">
              <path d="M0 46 L32 -10 L64 46 Z M56 46 L88 -10 L120 46 Z" fill="none" stroke="#FFFFFF" strokeOpacity="0.2" strokeWidth="1.5" strokeLinejoin="round" />
            </pattern>
          </defs>
          <rect width="100%" height="40" fill="url(#footer-triangles)" />
        </svg>
      </div>
    </footer>
  );
}
