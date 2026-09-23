import type { ButtonHTMLAttributes } from "react";
import { Link, type LinkProps } from "react-router";

type Variant = "primary" | "outline";

const BASE =
  "inline-flex h-12 items-center justify-center gap-2 rounded-md px-[22px] text-base font-bold no-underline transition-colors disabled:cursor-not-allowed disabled:opacity-60 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-accent";

const VARIANTS: Record<Variant, string> = {
  primary: "bg-brand-dark text-white hover:bg-[#004b59]",
  outline: "border border-brand-dark bg-white text-brand-dark hover:bg-brand-soft",
};

export function buttonClass(variant: Variant = "primary", extra = "") {
  return `${BASE} ${VARIANTS[variant]} ${extra}`;
}

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & { variant?: Variant };

export function Button({ variant = "primary", className = "", type = "button", ...props }: ButtonProps) {
  return <button type={type} className={buttonClass(variant, className)} {...props} />;
}

export function ButtonLink({ variant = "primary", className = "", ...props }: LinkProps & { variant?: Variant }) {
  return <Link className={buttonClass(variant, className)} {...props} />;
}
