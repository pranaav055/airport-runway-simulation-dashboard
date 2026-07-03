import "../App.css";

export default function DashboardHeader({ onHistory }) {
  return (
    <div className="dashboardHeader">
      <div className="brandBlock">
        <div className="brandMark" aria-hidden="true">✈︎</div>
        <div>
          <h1>Airport Simulation Dashboard</h1>
          <p>
            Configure traffic flow, runway modes, and operational policies before running the
            simulation.
          </p>
        </div>
      </div>
      <div className="headerActions">
        <button className="btn btnSecondary" type="button" onClick={onHistory}>
          History
        </button>
      </div>
    </div>
  );
}
