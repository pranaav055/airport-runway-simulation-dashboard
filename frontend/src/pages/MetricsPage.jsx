import { useNavigate, useLocation } from "react-router-dom";
import InsightsCard from "../components/InsightsCard.jsx";
import MetricCard from "../components/MetricCard.jsx";
import ResultsHeader from "../components/ResultsHeader.jsx";
import RunwayUtilizationCard from "../components/RunwayUtilizationCard.jsx";
import TrafficSnapshotCard from "../components/TrafficSnapshotCard.jsx";
import "../App.css";

export default function MetricsPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const result = location.state?.simulationResult;
  const config = location.state?.config;

  function handleSaveResults() {
    if (!result) return;

    const report = `Airport Simulation Results
--------------------------
Departure Metrics:
- Avg wait: ${result.avgTakeoffQueueWaitMinutes?.toFixed(1) || 0} min
- Max wait: ${result.maxDepartureDelayMinutes || 0} min
- Max queue: ${result.maxTakeoffQueueSize || 0} aircraft

Arrival Metrics:
- Avg holding: ${result.avgHoldingTimeMinutes?.toFixed(1) || 0} min
- Max holding: ${result.maxArrivalDelayMinutes || 0} min
- Max holding size: ${result.maxHoldingPatternSize || 0} aircraft

Disruptions:
- Cancelled flights: ${result.cancelledCount || 0}
- Diverted flights: ${result.divertedCount || 0}
`;

    const blob = new Blob([report], { type: "text/plain" });
    const url = URL.createObjectURL(blob);
    
    const link = document.createElement("a");
    link.href = url;
    link.download = "simulation-results.txt";
    
    document.body.appendChild(link);
    link.click();
    
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  if (!result) {
    return (
      <div className="page">
        <div className="content">
          <div className="emptyState">
            <h2>No Simulation Data</h2>
            <p>Please run a simulation from the configuration page first.</p>
            <button className="btn btnPrimary" onClick={() => navigate("/")}>
              Go to Configuration
            </button>
          </div>
        </div>
      </div>
    );
  }

  const runways = config?.runways || [];
  const inboundRate = config?.inboundFlowPerHour || 0;
  const outboundRate = config?.outboundFlowPerHour || 0;
  const activeRunways = runways.filter((r) => r.status === "Enabled").length;
  const emergencyConfig = config?.emergencyConfig || {};
  const fuelEmergencyCount = emergencyConfig.fuelEmergencyCount || 0;
  const medicalEmergencyCount = emergencyConfig.medicalEmergencyCount || 0;
  const technicalEmergencyCount = emergencyConfig.technicalEmergencyCount || 0;

  return (
    <div className="page">
      <div className="content">
        <ResultsHeader
          onBack={() => navigate("/")}
          onSave={handleSaveResults}
          onExit={() => navigate("/")}
        />

        <div className="resultsGrid">
          <div className="headlineGrid">
            <MetricCard
              title="Max Take-off Queue Length"
              value={result.maxTakeoffQueueSize || 0}
              decimals={0}
              suffix="aircraft"
              icon="🧱"
            />
            <MetricCard
              title="Avg Take-off Wait Time"
              value={result.avgTakeoffQueueWaitMinutes || 0}
              decimals={1}
              suffix="min"
              icon="⏱"
            />
            <MetricCard
              title="Max Holding Size"
              value={result.maxHoldingPatternSize || 0}
              decimals={0}
              suffix="aircraft"
              icon="🌀"
            />
            <MetricCard
              title="Avg Holding Time"
              value={result.avgHoldingTimeMinutes || 0}
              decimals={1}
              suffix="min"
              icon="🕒"
            />
            <MetricCard
              title="Cancellations"
              value={result.cancelledCount || 0}
              decimals={0}
              suffix=""
              icon="❌"
            />
            <MetricCard
              title="Diversions"
              value={result.divertedCount || 0}
              decimals={0}
              suffix=""
              icon="↗"
            />
          </div>

          <div className="visualGrid">
            <RunwayUtilizationCard
              runways={runways}
              inboundRate={inboundRate}
              outboundRate={outboundRate}
              maxTakeoffQueue={result.maxTakeoffQueueSize || 0}
              maxHoldingSize={result.maxHoldingPatternSize || 0}
            />
            <TrafficSnapshotCard
              inboundRate={inboundRate}
              outboundRate={outboundRate}
              activeRunways={activeRunways}
              totalRunways={runways.length}
              maxTakeoffQueue={result.maxTakeoffQueueSize || 0}
              maxHoldingSize={result.maxHoldingPatternSize || 0}
            />
          </div>

          <div className="insightsGrid">
            <div className="card configRecap">
              <div className="cardHeader">
                <h2>Configuration Recap</h2>
                <span className="cardSubtitle">Inputs used for this simulation</span>
              </div>
              {config ? (
                <div className="recapGrid">
                  <div>
                    <span>Duration</span>
                    <b>{config.durationMinutes} min</b>
                  </div>
                  <div>
                    <span>Inbound rate</span>
                    <b>{config.inboundFlowPerHour} vph</b>
                  </div>
                  <div>
                    <span>Outbound rate</span>
                    <b>{config.outboundFlowPerHour} vph</b>
                  </div>
                  <div>
                    <span>Runways</span>
                    <b>{runways.length}</b>
                  </div>
                  <div>
                    <span>Emergency priority</span>
                    <b>{config.emergencyPriority ? "Enabled" : "Disabled"}</b>
                  </div>
                  <div>
                    <span>Cancellation threshold</span>
                    <b>{config.cancellationThresholdMinutes} min</b>
                  </div>
                  <div>
                    <span>Fuel emergencies</span>
                    <b>{fuelEmergencyCount}</b>
                  </div>
                  <div>
                    <span>Medical emergencies</span>
                    <b>{medicalEmergencyCount}</b>
                  </div>
                  <div>
                    <span>Technical emergencies</span>
                    <b>{technicalEmergencyCount}</b>
                  </div>
                </div>
              ) : (
                <div className="recapEmpty">Configuration data unavailable.</div>
              )}
            </div>

            <InsightsCard
              inboundRate={inboundRate}
              outboundRate={outboundRate}
              activeRunways={activeRunways}
              totalRunways={runways.length}
              avgTakeoffWait={result.avgTakeoffQueueWaitMinutes || 0}
              avgHolding={result.avgHoldingTimeMinutes || 0}
              cancellations={result.cancelledCount || 0}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
