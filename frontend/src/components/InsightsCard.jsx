import "../App.css";

function buildInsights({ inboundRate = 0, outboundRate = 0, activeRunways = 0, totalRunways = 0, avgTakeoffWait = 0, avgHolding = 0, cancellations = 0 }) {
  const insights = [];

  if (inboundRate > outboundRate + 30) {
    insights.push("Inbound traffic exceeds outbound traffic, increasing holding pressure.");
  } else if (outboundRate > inboundRate + 30) {
    insights.push("Outbound demand is higher than inbound flow, raising take-off congestion risk.");
  } else {
    insights.push("Traffic flow appears balanced between arrivals and departures.");
  }

  if (totalRunways > 0 && activeRunways < totalRunways) {
    insights.push("One or more runways are closed, reducing airport throughput.");
  }

  if (avgTakeoffWait >= 10) {
    insights.push("Take-off waits are elevated, which may increase cancellations.");
  }

  if (avgHolding >= 8) {
    insights.push("Holding times are high, indicating inbound congestion.");
  }

  if (cancellations > 0) {
    insights.push("Cancellations recorded; review outbound scheduling and runway availability.");
  }

  return insights.slice(0, 4);
}

export default function InsightsCard(props) {
  const insights = buildInsights(props);

  return (
    <div className="card insightsCard">
      <div className="cardHeader">
        <h2>Simulation Insights</h2>
        <span className="cardSubtitle">Interpretation and operational takeaways</span>
      </div>
      <div className="insightsList">
        {insights.map((text, idx) => (
          <div className="insightItem" key={idx}>
            <span className="insightBullet" aria-hidden="true">•</span>
            <p>{text}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
