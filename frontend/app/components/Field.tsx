import type { InputHTMLAttributes, ReactNode, TextareaHTMLAttributes } from "react";

const CONTROL =
  "w-full rounded-md border border-line bg-white px-3.5 text-base text-ink placeholder:text-muted/70 focus:border-brand-dark focus:outline-2 focus:outline-brand/40";

type FieldProps = { label: string; hint?: ReactNode; name: string };

export function TextField({ label, hint, name, ...props }: FieldProps & InputHTMLAttributes<HTMLInputElement>) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="text-[15px] font-semibold text-ink">{label}</span>
      <input name={name} className={`${CONTROL} h-12`} {...props} />
      {hint && <span className="text-sm text-muted">{hint}</span>}
    </label>
  );
}

export function TextAreaField({ label, hint, name, ...props }: FieldProps & TextareaHTMLAttributes<HTMLTextAreaElement>) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="text-[15px] font-semibold text-ink">{label}</span>
      <textarea name={name} className={`${CONTROL} min-h-28 py-3`} {...props} />
      {hint && <span className="text-sm text-muted">{hint}</span>}
    </label>
  );
}
