import "../App.css";

export default function IllustrationPanel() {
  return (
    <div className="illustrationPanel" aria-hidden="true">
      <div className="illustrationSky" />
      <div className="illustrationRunway">
        <div className="runwayLine" />
      </div>
      <div className="illustrationPlane">✈︎</div>
      <div className="illustrationBeacon" />
    </div>
  );
}
