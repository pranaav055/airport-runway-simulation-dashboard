import "../App.css";

function clamp(n, min, max) {
  return Math.min(max, Math.max(min, n));
}

function computeRunwayUtilization(runway, index, baseLoad, congestionBoost) {
  if (runway.status === "Closed") return 0;
  const seed = 6 + (index * 7) % 13;
  return clamp(baseLoad + congestionBoost - seed, 35, 98);
}

export default function RunwayUtilizationCard({ runways = [], inboundRate = 0, outboundRate = 0, maxTakeoffQueue = 0, maxHoldingSize = 0 }) {
  const trafficLoad = (inboundRate + outboundRate) / 4;
  const congestionBoost = Math.min(20, (maxTakeoffQueue + maxHoldingSize) * 1.5);

  return (
    <div className="card runwayUtilCard">
      <div className="cardHeader">
        <h2>Runway Utilisation</h2>
        <span className="cardSubtitle">Estimated throughput and activity</span>
      </div>
      <div className="runwayUtilList">
        {runways.map((r, idx) => {
          const util = computeRunwayUtilization(r, idx, trafficLoad, congestionBoost);
          const isClosed = r.status === "Closed";
          return (
            <div key={r.id} className={`runwayUtilRow ${isClosed ? "isClosed" : ""}`}>
              <div className="runwayUtilLabel">
                <span className="runwayUtilNumber">Runway {r.id}</span>
                <span className="runwayUtilMeta">{r.mode} · {r.status}</span>
              </div>
              <div className="runwayUtilBar">
                <div className="runwayUtilFill" style={{ width: `${util}%` }} />
              </div>
              <div className="runwayUtilValue">{isClosed ? "Closed" : `${util}%`}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
