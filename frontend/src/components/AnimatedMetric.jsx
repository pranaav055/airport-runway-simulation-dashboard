import { useEffect, useRef, useState } from "react";
import "../App.css";

function easeOutCubic(t) {
  return 1 - Math.pow(1 - t, 3);
}

export default function AnimatedMetric({
  label,
  value,
  decimals = 0,
  suffix = "",
  duration = 1200,
}) {
  const [displayValue, setDisplayValue] = useState(0);
  const rafRef = useRef(null);

  useEffect(() => {
    const target = Number(value) || 0;
    const start = performance.now();

    const tick = (now) => {
      const elapsed = now - start;
      const t = Math.min(1, elapsed / duration);
      const eased = easeOutCubic(t);
      const nextValue = target * eased;
      setDisplayValue(nextValue);

      if (t < 1) {
        rafRef.current = requestAnimationFrame(tick);
      } else {
        setDisplayValue(target);
      }
    };

    rafRef.current = requestAnimationFrame(tick);

    return () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
    };
  }, [value, duration]);

  const formatted =
    decimals > 0 ? displayValue.toFixed(decimals) : Math.round(displayValue).toString();

  return (
    <div className={`metricRow metricRowAnimated ${label ? "" : "metricRowNoLabel"}`.trim()}>
      {label ? <span>{label}</span> : <span className="srOnly">Metric</span>}
      <b className="metricValueAnimated">
        {formatted}
        {suffix ? <span className="metricSuffix"> {suffix}</span> : null}
      </b>
    </div>
  );
}
