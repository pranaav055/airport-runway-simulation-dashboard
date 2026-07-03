import "../App.css";

export default function HistoryPanel({ isOpen, onClose, entries = [], onSelect }) {
  if (!isOpen) return null;

  return (
    <div className="historyOverlay" role="dialog" aria-modal="true">
      <div className="historyPanel">
        <div className="historyHeader">
          <div>
            <h2>Simulation History</h2>
            <p>Recent runs saved locally on this device</p>
          </div>
          <button className="btn btnSecondary" type="button" onClick={onClose}>
            Close
          </button>
        </div>

        {entries.length === 0 ? (
          <div className="emptyState">
            <h3>No saved simulations yet</h3>
            <p>Run a simulation to create a history entry.</p>
          </div>
        ) : (
          <div className="historyList">
            {entries.map((entry) => (
              <button
                key={entry.id}
                type="button"
                className="historyItem"
                onClick={() => onSelect(entry)}
              >
                <div className="historyItemTop">
                  <span className="historyTitle">Run {entry.runNumber}</span>
                  <span className="historyTime">{entry.timestamp}</span>
                </div>
                <div className="historyMeta">
                  <span>Inbound {entry.config?.inboundFlowPerHour} vph</span>
                  <span>Outbound {entry.config?.outboundFlowPerHour} vph</span>
                  <span>Runways {entry.config?.runways?.length || 0}</span>
                </div>
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
