import "../App.css";

export default function SectionCard({ title, subtitle, icon, children }) {
  return (
    <div className="card sectionCard">
      <div className="sectionCardHeader">
        <span className="sectionIcon" aria-hidden="true">{icon}</span>
        <div>
          <h2>{title}</h2>
          <p>{subtitle}</p>
        </div>
      </div>
      <div className="sectionCardBody">{children}</div>
    </div>
  );
}
