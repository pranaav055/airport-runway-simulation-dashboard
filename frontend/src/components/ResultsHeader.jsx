import "../App.css";

export default function ResultsHeader({ onBack, onSave, onExit }) {
  return (
    <div className="resultsHeaderBar">
      <div>
        <h1>Simulation Results</h1>
        <p>Performance summary for the current airport configuration</p>
      </div>
      <div className="resultsHeaderActions">
        <button className="btn btnSecondary" type="button" onClick={onBack}>
          Back
        </button>
        <button className="btn btnPrimary" type="button" onClick={onSave}>
          Save Results
        </button>
        <button className="btn btnDanger" type="button" onClick={onExit}>
          Exit
        </button>
      </div>
    </div>
  );
}
