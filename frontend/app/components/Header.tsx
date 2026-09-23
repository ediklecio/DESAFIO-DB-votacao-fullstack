import { Link, useLocation, useSearchParams } from "react-router";
import { Container } from "./Container";
import { LogoIcon } from "./Icons";

type Section = "pautas" | "nova" | "resultados";

function useActiveSection(): Section {
  const { pathname } = useLocation();
  const [searchParams] = useSearchParams();
  if (pathname === "/pautas/nova") return "nova";
  if (pathname === "/" && searchParams.get("filtro") === "encerradas") return "resultados";
  return "pautas";
}

const NAV_ITEMS: { section: Section; label: string; to: string }[] = [
  { section: "pautas", label: "Pautas", to: "/" },
  { section: "nova", label: "Nova pauta", to: "/pautas/nova" },
  { section: "resultados", label: "Resultados", to: "/?filtro=encerradas" },
];

export function Header() {
  const active = useActiveSection();

  return (
    <header className="bg-brand-dark text-white">
      <Container className="flex flex-wrap items-center justify-between gap-x-6 md:h-[72px]">
        <Link to="/" className="flex items-center gap-3 py-4 text-white no-underline md:py-0">
          <LogoIcon />
          <span className="text-lg font-bold sm:text-xl">Votação Cooperativa</span>
        </Link>
        <nav aria-label="Principal" className="-mx-1 flex w-full items-center gap-6 overflow-x-auto md:mx-0 md:w-auto md:gap-10">
          {NAV_ITEMS.map((item) => {
            const isActive = item.section === active;
            return (
              <Link
                key={item.section}
                to={item.to}
                aria-current={isActive ? "page" : undefined}
                className={`whitespace-nowrap border-b-[3px] px-1 pt-3 pb-[9px] text-base text-white no-underline md:pt-6 md:pb-[21px] ${
                  isActive ? "border-accent font-semibold" : "border-transparent font-medium hover:border-white/40"
                }`}
              >
                {item.label}
              </Link>
            );
          })}
        </nav>
      </Container>
    </header>
  );
}
