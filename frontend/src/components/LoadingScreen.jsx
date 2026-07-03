import { useEffect, useMemo, useRef, useState } from "react";
import "../App.css";

const STAGES = [
  "Initializing simulation...",
  "Loading runway configuration...",
  "Generating aircraft schedules...",
  "Running traffic model...",
  "Computing metrics...",
];

export default function LoadingScreen({ onComplete, durationMs = 3000 }) {
  const [progress, setProgress] = useState(0);
  const [isComplete, setIsComplete] = useState(false);
  const rafRef = useRef(null);
  const completeRef = useRef(false);
  const completeTimerRef = useRef(null);

  const stageText = useMemo(() => {
    if (isComplete) return "Simulation complete";
    const idx = Math.min(STAGES.length - 1, Math.floor(progress / 20));
    return STAGES[idx];
  }, [isComplete, progress]);

  useEffect(() => {
    const start = performance.now();

    const tick = (now) => {
      const elapsed = now - start;
      const pct = Math.min(100, (elapsed / durationMs) * 100);
      setProgress(pct);

      if (pct < 100) {
        rafRef.current = requestAnimationFrame(tick);
      } else if (!completeRef.current) {
        completeRef.current = true;
        setIsComplete(true);
        completeTimerRef.current = setTimeout(() => {
          if (onComplete) onComplete();
        }, 350);
      }
    };

    rafRef.current = requestAnimationFrame(tick);

    return () => {
      if (rafRef.current) cancelAnimationFrame(rafRef.current);
      if (completeTimerRef.current) clearTimeout(completeTimerRef.current);
    };
  }, [durationMs, onComplete]);

  return (
    <div className="loadingScreen">
      <div className="loadingCard">
        <div className="loadingHeader">Running Simulation</div>
        <div className="loadingBody">
          <div className="loadingStage">{stageText}</div>

          <div className="progressBar">
            <div className="progressFill" style={{ width: `${progress}%` }} />
          </div>
          <div className="progressText">{Math.round(progress)}%</div>

          <div className="loadingRunway">
            <span className="loadingPlane">✈️</span>
          </div>
        </div>
      </div>
    </div>
  );
}
