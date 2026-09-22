import { useEffect, useRef, useState } from "react";

/**
 * Ticks down from the server-provided secondsRemaining once per second and
 * calls onExpire once when it reaches zero. Restarts whenever the input changes
 * (e.g. after the route data is revalidated).
 */
export function useCountdown(secondsRemaining: number, onExpire?: () => void) {
  const [remaining, setRemaining] = useState(secondsRemaining);
  const onExpireRef = useRef(onExpire);
  onExpireRef.current = onExpire;

  useEffect(() => {
    setRemaining(secondsRemaining);
    if (secondsRemaining <= 0) {
      // Server rounds down: an OPEN session may report 0s for its last fraction
      // of a second. Re-check shortly instead of showing 00:00 forever.
      const retry = setTimeout(() => onExpireRef.current?.(), 1000);
      return () => clearTimeout(retry);
    }

    const deadline = Date.now() + secondsRemaining * 1000;
    const timer = setInterval(() => {
      const left = Math.max(0, Math.ceil((deadline - Date.now()) / 1000));
      setRemaining(left);
      if (left === 0) {
        clearInterval(timer);
        onExpireRef.current?.();
      }
    }, 1000);
    return () => clearInterval(timer);
  }, [secondsRemaining]);

  return remaining;
}
