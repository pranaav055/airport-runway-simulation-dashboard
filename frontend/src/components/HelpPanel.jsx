import "../App.css";

export default function HelpPanel({ isOpen, onClose }) {
  if (!isOpen) return null;

  return (
    <div className="modalOverlay" role="dialog" aria-modal="true">
      <div className="helpPanel">
        <div className="helpHeader">
          <div>
            <h2>Simulation Help</h2>
            <p>Quick guide for configuring and running a simulation</p>
          </div>
          <button className="btn btnSecondary" type="button" onClick={onClose}>
            Close
          </button>
        </div>

        <div className="helpBody">
          <div>
            <h3>Traffic Configuration</h3>
            <p>Adjust inbound/outbound rates to change the load on the airport.</p>
          </div>
          <div>
            <h3>Runway Configuration</h3>
            <p>Select how each runway operates and enable or close as needed.</p>
          </div>
          <div>
            <h3>Operational Policies</h3>
            <p>Emergency priority and cancellation thresholds affect congestion outcomes.</p>
          </div>
          <div>
            <h3>Emergency Scenarios</h3>
            <p>
              Set how many inbound aircraft are emergencies. The total of fuel, medical, and
              technical emergencies cannot exceed the estimated inbound aircraft count
              (duration × inbound rate).
            </p>
          </div>
          <div>
            <h3>Run Simulation</h3>
            <p>Click Run Simulation to generate results and see performance metrics.</p>
          </div>
        </div>
      </div>
    </div>
  );
}
