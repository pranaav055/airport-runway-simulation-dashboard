import "../App.css";

function formatBool(value) {
  return value ? "Enabled" : "Disabled";
}

export default function SummaryCard({
  inboundRate,
  outboundRate,
  runwayCount,
  runways,
  emergencyPriority,
  cancelAfterMins,
  fuelEmergencyCount,
  medicalEmergencyCount,
  technicalEmergencyCount,
}) {
  const activeRunways = runways.filter((r) => r.status === "Enabled").length;
  const closedRunways = runwayCount - activeRunways;
  const totalEmergencies =
    (fuelEmergencyCount || 0) + (medicalEmergencyCount || 0) + (technicalEmergencyCount || 0);

  let statusLine = "Balanced traffic flow";
  if (activeRunways < runwayCount) {
    statusLine = "Reduced runway availability";
  } else if (inboundRate > outboundRate + 30) {
    statusLine = "High inbound traffic";
  } else if (outboundRate > inboundRate + 30) {
    statusLine = "High outbound traffic";
  }

  return (
    <div className="card summaryCard">
      <div className="summaryHeader">
        <h2>Simulation Summary</h2>
        <span className="summaryStatus">{statusLine}</span>
      </div>
      {totalEmergencies > 0 ? (
        <div className="summaryNote">
          Priority handling required for emergency inbound aircraft.
        </div>
      ) : null}
      <div className="summaryGrid">
        <div className="summaryItem">
          <span>Inbound rate</span>
          <b>{inboundRate} vph</b>
        </div>
        <div className="summaryItem">
          <span>Outbound rate</span>
          <b>{outboundRate} vph</b>
        </div>
        <div className="summaryItem">
          <span>Runways</span>
          <b>{runwayCount}</b>
        </div>
        <div className="summaryItem">
          <span>Active runways</span>
          <b>{activeRunways}</b>
        </div>
        <div className="summaryItem">
          <span>Closed runways</span>
          <b>{closedRunways}</b>
        </div>
        <div className="summaryItem">
          <span>Emergency priority</span>
          <b>{formatBool(emergencyPriority)}</b>
        </div>
        <div className="summaryItem">
          <span>Cancellation threshold</span>
          <b>{cancelAfterMins} min</b>
        </div>
        <div className="summaryItem">
          <span>Fuel emergencies</span>
          <b>{fuelEmergencyCount}</b>
        </div>
        <div className="summaryItem">
          <span>Medical emergencies</span>
          <b>{medicalEmergencyCount}</b>
        </div>
        <div className="summaryItem">
          <span>Technical emergencies</span>
          <b>{technicalEmergencyCount}</b>
        </div>
        <div className="summaryItem">
          <span>Total emergencies</span>
          <b>{totalEmergencies}</b>
        </div>
      </div>
    </div>
  );
}
