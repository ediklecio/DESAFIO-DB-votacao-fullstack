import { useCountdown } from "~/hooks/useCountdown";
import { formatCountdown } from "~/lib/agenda";

type CountdownProps = { seconds: number; onExpire?: () => void };

export function Countdown({ seconds, onExpire }: CountdownProps) {
  const remaining = useCountdown(seconds, onExpire);
  return <span className="tabular-nums">{formatCountdown(remaining)}</span>;
}
