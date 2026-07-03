import "../App.css";

function clamp(n, min, max) {
  return Math.min(max, Math.max(min, n));
}

function loadLabel(value) {
  if (value >= 140) return "High";
  if (value >= 80) return "Moderate";
  return "Low";
}

export default function TrafficSnapshotCard({ inboundRate = 0, outboundRate = 0, activeRunways = 0, totalRunways = 0, maxTakeoffQueue = 0, maxHoldingSize = 0 }) {
  const inboundLoad = loadLabel(inboundRate);
  const outboundLoad = loadLabel(outboundRate);
  const availability = totalRunways > 0 && activeRunways < totalRunways ? "Reduced" : "Normal";
  const congestionScore = clamp(maxTakeoffQueue + maxHoldingSize, 0, 30);
  const congestionRisk = congestionScore >= 18 ? "Elevated" : congestionScore >= 10 ? "Moderate" : "Low";

  const inboundPct = clamp((inboundRate / 200) * 100, 0, 100);
  const outboundPct = clamp((outboundRate / 200) * 100, 0, 100);

  return (
    <div className="card trafficSnapshot">
      <div className="cardHeader">
        <h2>Traffic Snapshot</h2>
        <span className="cardSubtitle">Current flow intensity and congestion</span>
      </div>

      <div className="trafficBars">
        <div className="trafficBarRow">
          <span>Inbound load</span>
          <div className="trafficBar">
            <div className="trafficFill inbound" style={{ width: `${inboundPct}%` }} />
          </div>
          <b>{inboundLoad}</b>
        </div>
        <div className="trafficBarRow">
          <span>Outbound load</span>
          <div className="trafficBar">
            <div className="trafficFill outbound" style={{ width: `${outboundPct}%` }} />
          </div>
          <b>{outboundLoad}</b>
        </div>
      </div>

      <div className="trafficStats">
        <div>
          <span>Runway availability</span>
          <b>{availability}</b>
        </div>
        <div>
          <span>Congestion risk</span>
          <b>{congestionRisk}</b>
        </div>
      </div>
    </div>
  );
}
