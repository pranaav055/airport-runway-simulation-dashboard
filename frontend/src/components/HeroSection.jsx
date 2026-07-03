import IllustrationPanel from "./IllustrationPanel.jsx";
import "../App.css";

export default function HeroSection({ onHistory, onHelp }) {
  return (
    <section className="heroSection">
      <div className="heroInner">
        <div className="brandBadge">AeroFlow Simulation</div>
        <div className="heroMain">
          <div className="heroText">
            <h1>Airport Simulation Dashboard</h1>
            <p>
              Configure runway operations, traffic flow, and disruption policies before running the
              simulation.
            </p>
            <div className="heroActions">
              <button className="btn btnSecondary" type="button" onClick={onHistory}>
                History
              </button>
              <button className="btn btnGhost" type="button" onClick={onHelp}>
                Help
              </button>
            </div>
          </div>
          <div className="heroVisual" aria-hidden="true">
            <IllustrationPanel />
          </div>
        </div>
      </div>
    </section>
  );
}
