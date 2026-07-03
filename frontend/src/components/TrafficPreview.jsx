import "../App.css";

function clamp(n, min, max) {
  return Math.min(max, Math.max(min, n));
}

export default function TrafficPreview({ direction = "inbound", trafficLevel = 0 }) {
  const maxPlanes = 6;
  const baseCount = trafficLevel <= 0 ? 0 : Math.ceil(trafficLevel / 35);
  const planeCount = clamp(baseCount, 0, maxPlanes);

  const duration = clamp(14 - trafficLevel / 25, 6, 14);
  const planes = Array.from({ length: planeCount }, (_, i) => i);

  return (
    <div className={`trafficPreview trafficPreview-${direction}`} aria-hidden="true">
      <div className="trafficLane">
        {planes.map((i) => (
          <span
            key={i}
            className="trafficPlane"
            style={{
              animationDuration: `${duration}s`,
              animationDelay: `-${(i * duration) / Math.max(1, planeCount)}s`,
            }}
          >
            ✈️
          </span>
        ))}
      </div>
    </div>
  );
}
