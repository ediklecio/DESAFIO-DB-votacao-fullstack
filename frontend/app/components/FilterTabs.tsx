export type FilterTab<T extends string> = { id: T; label: string; count: number };

type FilterTabsProps<T extends string> = {
  tabs: FilterTab<T>[];
  active: T;
  onChange: (id: T) => void;
};

export function FilterTabs<T extends string>({ tabs, active, onChange }: FilterTabsProps<T>) {
  return (
    <div role="group" aria-label="Filtrar pautas" className="flex flex-wrap gap-2">
      {tabs.map((tab) => {
        const isActive = tab.id === active;
        return (
          <button
            key={tab.id}
            type="button"
            aria-pressed={isActive}
            onClick={() => onChange(tab.id)}
            className={`flex h-11 cursor-pointer items-center gap-2 rounded-full border px-[18px] text-[15px] ${
              isActive
                ? "border-brand-dark bg-brand-dark font-semibold text-white"
                : "border-line bg-white font-medium text-brand-dark hover:border-brand-dark"
            }`}
          >
            {tab.label}
            <span
              className={`flex h-[22px] min-w-[22px] items-center justify-center rounded-full px-1.5 text-[13px] font-bold ${
                isActive ? "bg-brand text-brand-dark" : "bg-chip text-ink"
              }`}
            >
              {tab.count}
            </span>
          </button>
        );
      })}
    </div>
  );
}
