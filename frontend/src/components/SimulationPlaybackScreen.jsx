import { useEffect, useMemo, useRef, useState } from "react";
import "../App.css";

function clampInt(value, min, max) {
  return Math.min(max, Math.max(min, Math.round(value)));
}

function PlaneToken({ variant = "holding" }) {
  return <span className={`planeToken planeToken-${variant}`}>✈</span>;
}

export default function SimulationPlaybackScreen({ result, config, onComplete }) {
  const totalTicks = 16;
  const intervalMs = 450;
  const completionDelay = 700;
  const timerRef = useRef(null);
  const doneRef = useRef(false);

  const targets = useMemo(() => {
    const inbound = config?.inboundFlowPerHour ?? 50;
    const outbound = config?.outboundFlowPerHour ?? 40;
    const runways = config?.runways?.length ?? 3;
    const enabledRunways = config?.runways?.filter((r) => r.status === "Enabled").length ?? runways;

    const maxHolding = result?.maxHoldingPatternSize ?? Math.round(inbound / 6);
    const maxTakeoff = result?.maxTakeoffQueueSize ?? Math.round(outbound / 6);
    const holdingTarget = clampInt(maxHolding / 2 + inbound / 50 - enabledRunways / 2, 2, 10);
    const departureTarget = clampInt(maxTakeoff / 2 + outbound / 50 - enabledRunways / 2, 2, 10);

    const serviceBias = clampInt((enabledRunways / Math.max(1, runways)) * 3, 1, 3);
    return {
      holding: holdingTarget,
      departure: departureTarget,
      serviceBias,
      inbound,
      outbound,
      enabledRunways,
    };
  }, [config, result]);

  const [tick, setTick] = useState(0);
  const [phase, setPhase] = useState("landing");
  const [holdingCount, setHoldingCount] = useState(0);
  const [departureCount, setDepartureCount] = useState(0);
  const [runwayActive, setRunwayActive] = useState(false);
  const [complete, setComplete] = useState(false);

  useEffect(() => {
    if (!result) return;
    doneRef.current = false;
    setTick(0);
    setPhase("landing");
    setHoldingCount(targets.holding);
    setDepartureCount(targets.departure);
    setRunwayActive(false);
    setComplete(false);

    if (timerRef.current) clearInterval(timerRef.current);
    timerRef.current = setInterval(() => {
      setTick((prev) => prev + 1);
      setRunwayActive(true);
      setPhase((prevPhase) => {
        const nextPhase = prevPhase === "landing" ? "takeoff" : "landing";
        setHoldingCount((prev) => {
          let next = prev;
          if (nextPhase === "landing" && prev > 0) next -= targets.serviceBias > 2 ? 2 : 1;
          if (next < targets.holding && Math.random() > 0.35) next += 1;
          return clampInt(next, 0, targets.holding);
        });
        setDepartureCount((prev) => {
          let next = prev;
          if (nextPhase === "takeoff" && prev > 0) next -= targets.serviceBias > 2 ? 2 : 1;
          if (next < targets.departure && Math.random() > 0.4) next += 1;
          return clampInt(next, 0, targets.departure);
        });
        return nextPhase;
      });
    }, intervalMs);

    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [result, targets.departure, targets.holding]);

  useEffect(() => {
    if (!result) return;
    if (tick >= totalTicks && !doneRef.current) {
      doneRef.current = true;
      setComplete(true);
      if (timerRef.current) clearInterval(timerRef.current);
      setRunwayActive(false);
      const endTimer = setTimeout(() => {
        onComplete?.();
      }, completionDelay);
      return () => clearTimeout(endTimer);
    }
  }, [tick, result, onComplete]);

  const progress = Math.min(100, Math.round((tick / totalTicks) * 100));
  const simulatedMinutes = clampInt((tick / totalTicks) * 90, 5, 90);

  if (!result) {
    return (
      <div className="playbackScreen">
        <div className="playbackCard">
          <h2>Running Simulation</h2>
          <p>Preparing simulation playback…</p>
          <div className="playbackProgress">
            <div className="playbackProgressBar" style={{ width: "35%" }} />
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="playbackScreen">
      <div className="playbackCard">
        <div className="playbackHeader">
          <div>
            <h2>Running Simulation</h2>
            <p>Visualising aircraft flow and runway activity</p>
          </div>
          <div className="playbackTime">Time: 00:{String(simulatedMinutes).padStart(2, "0")}</div>
        </div>

        <div className="playbackLanes">
          <div className="playbackLane">
            <div className="laneHeader">
              <span>Holding Pattern</span>
              <span className="laneCount">{holdingCount} aircraft</span>
            </div>
            <div className="laneTrack">
              {Array.from({ length: holdingCount }).map((_, idx) => (
                <PlaneToken key={`h-${idx}`} variant="holding" />
              ))}
            </div>
          </div>

          <div className="playbackLane runwayLane">
            <div className="laneHeader">
              <span>Runway</span>
              <span className="laneCount">
                {runwayActive ? (phase === "landing" ? "Landing in progress" : "Take-off in progress") : "Idle"}
              </span>
            </div>
            <div className="laneTrack runwayTrack">
              {runwayActive ? <PlaneToken variant="runway" /> : <span className="runwayIdle" />}
            </div>
          </div>

          <div className="playbackLane">
            <div className="laneHeader">
              <span>Departure Queue</span>
              <span className="laneCount">{departureCount} aircraft</span>
            </div>
            <div className="laneTrack">
              {Array.from({ length: departureCount }).map((_, idx) => (
                <PlaneToken key={`d-${idx}`} variant="departure" />
              ))}
            </div>
          </div>
        </div>

        <div className="playbackFooter">
          <div className="playbackProgress">
            <div className="playbackProgressBar" style={{ width: `${progress}%` }} />
          </div>
          <div className="playbackStatus">{complete ? "Simulation complete" : `${progress}%`}</div>
        </div>
      </div>
    </div>
  );
}
