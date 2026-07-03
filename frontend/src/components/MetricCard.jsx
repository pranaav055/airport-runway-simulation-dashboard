import AnimatedMetric from "./AnimatedMetric.jsx";
import "../App.css";

export default function MetricCard({ title, value, decimals = 0, suffix = "", icon }) {
  return (
    <div className="metricCard">
      <div className="metricCardTop">
        <span className="metricCardIcon" aria-hidden="true">{icon}</span>
        <span className="metricCardTitle">{title}</span>
      </div>
      <AnimatedMetric
        label={""}
        value={value}
        decimals={decimals}
        suffix={suffix}
        duration={1300}
      />
    </div>
  );
}
