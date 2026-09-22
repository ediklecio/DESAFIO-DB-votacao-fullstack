type IconProps = { size?: number; className?: string };

export function LogoIcon({ size = 32 }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 32 32" fill="none" strokeWidth="2" strokeLinejoin="round" strokeLinecap="round" aria-hidden="true">
      <path d="M16 4 L29 27 L3 27 Z" stroke="#7DB61C" />
      <path d="M11 19 L15 23 L21 15" stroke="#FFFFFF" />
    </svg>
  );
}

export function PlusIcon({ size = 18, className }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true" className={className}>
      <path d="M12 5v14M5 12h14" />
    </svg>
  );
}

export function ChevronRightIcon({ size = 16, className }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" className={className}>
      <path d="M9 6l6 6-6 6" />
    </svg>
  );
}

export function ChevronLeftIcon({ size = 16, className }: IconProps) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" className={className}>
      <path d="M15 6l-6 6 6 6" />
    </svg>
  );
}

/** Decorative outlined triangles used on the hero and footer bands. */
export function TrianglesPattern() {
  return (
    <svg
      className="pointer-events-none absolute inset-y-0 right-0 h-full w-auto"
      viewBox="640 0 640 200"
      preserveAspectRatio="xMaxYMid slice"
      fill="none"
      stroke="#FFFFFF"
      strokeOpacity="0.2"
      strokeWidth="1.5"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M640 210 L740 30 L840 210 Z" />
      <path d="M720 150 L780 45 L840 150 Z" />
      <path d="M800 230 L930 -10 L1060 230 Z" />
      <path d="M960 170 L1040 30 L1120 170 Z" />
      <path d="M1080 230 L1190 40 L1300 230 Z" />
      <path d="M1150 120 L1210 15 L1270 120 Z" />
    </svg>
  );
}
