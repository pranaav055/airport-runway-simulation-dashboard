import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import HeroSection from "../components/HeroSection.jsx";
import HistoryPanel from "../components/HistoryPanel.jsx";
import HelpPanel from "../components/HelpPanel.jsx";
import SimulationPlaybackScreen from "../components/SimulationPlaybackScreen.jsx";
import SectionCard from "../components/SectionCard.jsx";
import SummaryCard from "../components/SummaryCard.jsx";
import "../App.css";

const RUNWAY_MODES = ["Landing", "Take-off", "Mixed"];
const RUNWAY_STATUSES = ["Enabled", "Closed"];

function clamp(n, lo, hi) {
  return Math.min(hi, Math.max(lo, n));
}

function makeRunways(n) {
  return Array.from({ length: n }, (_, i) => ({
    id: i + 1,
    mode: i === 0 ? "Landing" : i === 1 ? "Take-off" : "Mixed",
    status: "Enabled",
  }));
}

export default function ConfigPage() {
  const navigate = useNavigate();

  const [durationMins, setDurationMins] = useState(1440);
  const [inboundRate, setInboundRate] = useState(50);
  const [outboundRate, setOutboundRate] = useState(40);
  const [runwayCount, setRunwayCount] = useState(3);
  const [runways, setRunways] = useState(() => makeRunways(3));
  const [emergencyPriority, setEmergencyPriority] = useState(true);
  const [cancelAfterMins, setCancelAfterMins] = useState(30);
  const [fuelEmergencyCount, setFuelEmergencyCount] = useState(0);
  const [medicalEmergencyCount, setMedicalEmergencyCount] = useState(0);
  const [technicalEmergencyCount, setTechnicalEmergencyCount] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [isStarting, setIsStarting] = useState(false);
  const [loadingDone, setLoadingDone] = useState(false);
  const [resultData, setResultData] = useState(null);
  const [lastConfig, setLastConfig] = useState(null);
  const [errorMessage, setErrorMessage] = useState("");
  const [historyOpen, setHistoryOpen] = useState(false);
  const [helpOpen, setHelpOpen] = useState(false);
  const [historyEntries, setHistoryEntries] = useState([]);
  const startTimerRef = useRef(null);

  const estimatedInboundAircraft = Math.floor((durationMins / 60) * inboundRate);
  const totalEmergencyCount =
    fuelEmergencyCount + medicalEmergencyCount + technicalEmergencyCount;
  const hasNegativeEmergency =
    fuelEmergencyCount < 0 || medicalEmergencyCount < 0 || technicalEmergencyCount < 0;
  const hasEmergencyValidationError =
    hasNegativeEmergency || totalEmergencyCount > estimatedInboundAircraft;
  let emergencyValidationMessage = "";
  if (hasNegativeEmergency) {
    emergencyValidationMessage = "Emergency counts cannot be negative.";
  } else if (totalEmergencyCount > estimatedInboundAircraft) {
    emergencyValidationMessage =
      "Total emergency aircraft cannot exceed the estimated number of inbound aircraft.";
  }

  useEffect(() => {
    const stored = localStorage.getItem("simHistory");
    if (stored) {
      try {
        setHistoryEntries(JSON.parse(stored));
      } catch {
        setHistoryEntries([]);
      }
    }
  }, []);

  function saveHistoryEntry(result, config) {
    const timestamp = new Date().toLocaleString();
    const nextEntry = {
      id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
      runNumber: historyEntries.length + 1,
      timestamp,
      config,
      result,
    };
    const updated = [nextEntry, ...historyEntries].slice(0, 8);
    setHistoryEntries(updated);
    localStorage.setItem("simHistory", JSON.stringify(updated));
  }

  function handleHistorySelect(entry) {
    setHistoryOpen(false);
    navigate("/metrics", { state: { simulationResult: entry.result, config: entry.config } });
  }

  useEffect(() => {
    setRunways((prev) => {
      if (prev.length === runwayCount) return prev;

      if (prev.length < runwayCount) {
        const toAdd = runwayCount - prev.length;
        const newOnes = Array.from({ length: toAdd }, (_, idx) => ({
          id: prev.length + idx + 1,
          mode: "Mixed",
          status: "Enabled",
        }));
        return [...prev, ...newOnes];
      }

      return prev.slice(0, runwayCount);
    });
  }, [runwayCount]);

  function updateRunway(id, patch) {
    setRunways((prev) => prev.map((r) => (r.id === id ? { ...r, ...patch } : r)));
  }

  function onReset() {
    setDurationMins(1440);
    setInboundRate(50);
    setOutboundRate(40);
    setRunwayCount(3);
    setRunways(makeRunways(3));
    setEmergencyPriority(true);
    setCancelAfterMins(30);
    setFuelEmergencyCount(0);
    setMedicalEmergencyCount(0);
    setTechnicalEmergencyCount(0);
  }

  useEffect(() => {
    return () => {
      if (startTimerRef.current) clearTimeout(startTimerRef.current);
    };
  }, []);

  useEffect(() => {
    if (loadingDone && resultData) {
      navigate("/metrics", { state: { simulationResult: resultData, config: lastConfig } });
    }
  }, [lastConfig, loadingDone, navigate, resultData]);

  async function handleRunSimulation() {
    if (isLoading || isStarting || hasEmergencyValidationError) return;

    const configPayload = {
      durationMinutes: durationMins,
      inboundFlowPerHour: inboundRate,
      outboundFlowPerHour: outboundRate,
      cancellationThresholdMinutes: cancelAfterMins,
      randomSeed: Math.floor(Math.random() * 100000), 
      runways: runways,
      emergencyPriority: emergencyPriority,
      emergencyConfig: {
        fuelEmergencyCount,
        medicalEmergencyCount,
        technicalEmergencyCount,
      },
    };

    try {
      setIsStarting(true);
      setErrorMessage("");
      setLastConfig(configPayload);
      setLoadingDone(false);
      setResultData(null);
      if (startTimerRef.current) clearTimeout(startTimerRef.current);
      startTimerRef.current = setTimeout(() => {
        setIsLoading(true);
        setIsStarting(false);
      }, 200);

      const response = await fetch("http://localhost:8080/api/simulation/run", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(configPayload),
      });

      if (!response.ok) {
        throw new Error(`Backend error: ${response.statusText}`);
      }

      const resultData = await response.json();
      setResultData(resultData);
      saveHistoryEntry(resultData, configPayload);

    } catch (error) {
      console.error("Failed to fetch simulation results:", error);
      if (startTimerRef.current) clearTimeout(startTimerRef.current);
      setIsStarting(false);
      setIsLoading(false);
      setLoadingDone(false);
      setErrorMessage("Simulation failed to run. Please verify the backend is running and try again.");
    }
  }

  if (isLoading) {
    return (
      <SimulationPlaybackScreen
        result={resultData}
        config={lastConfig}
        onComplete={() => setLoadingDone(true)}
      />
    );
  }

  return (
    <div className="page">
      <div className="content">
        <HeroSection
          onHistory={() => setHistoryOpen(true)}
          onHelp={() => setHelpOpen(true)}
        />

        {errorMessage ? (
          <div className="errorCard">
            <div className="errorTitle">Simulation Failed</div>
            <p>{errorMessage}</p>
            <button className="btn btnSecondary" type="button" onClick={() => setErrorMessage("")}>
              Dismiss
            </button>
          </div>
        ) : null}

        <div className="configLayout">
          <section className="section trafficWide">
            <SectionCard
              title="Traffic Configuration"
              subtitle="Set simulation duration and traffic intensity"
              icon="🛫"
            >
              <SliderRow
                label="Simulation Duration"
                value={durationMins}
                unit="min"
                min={60}
                max={2880}
                step={60}
                onChange={setDurationMins}
              />

              <SliderRow
                label="Inbound Rate"
                value={inboundRate}
                unit="vph"
                min={0}
                max={200}
                step={1}
                onChange={setInboundRate}
              />

              <SliderRow
                label="Outbound Rate"
                value={outboundRate}
                unit="vph"
                min={0}
                max={200}
                step={1}
                onChange={setOutboundRate}
              />
            </SectionCard>
          </section>

          <div className="configGrid">
            <div className="configLeft">
              <section className="section">
                <SectionCard
                  title="Operational Policies"
                  subtitle="Define emergency and cancellation behaviour"
                  icon="🛡️"
                >
                  <div className="row">
                    <label className="label">Emergency Priority:</label>
                    <Segmented
                      value={emergencyPriority ? "ON" : "OFF"}
                      options={["ON", "OFF"]}
                      onChange={(v) => setEmergencyPriority(v === "ON")}
                      accent={emergencyPriority ? "green" : "gray"}
                    />
                  </div>

                  <div className="row">
                    <label className="label">Cancel after:</label>
                    <div className="inline">
                      <input
                        className="input"
                        type="number"
                        min={0}
                        max={240}
                        value={cancelAfterMins}
                        onChange={(e) =>
                          setCancelAfterMins(
                            clamp(parseInt(e.target.value || "0", 10), 0, 240)
                          )
                        }
                      />
                      <span className="muted">minutes</span>
                    </div>
                  </div>
                </SectionCard>
              </section>

              <section className="section">
                <SectionCard
                  title="Emergency Scenarios"
                  subtitle="Configure the number of aircraft requiring priority handling."
                  icon="🚨"
                >
                  <div className="emergencyList">
                    <div className="emergencyRow">
                      <div className="emergencyMeta">
                        <span className="label">Fuel Emergencies</span>
                        <span className="helperText">Highest landing priority</span>
                      </div>
                      <input
                        className="input"
                        type="number"
                        min={0}
                        value={fuelEmergencyCount}
                        onChange={(e) =>
                          setFuelEmergencyCount(
                            Math.max(0, parseInt(e.target.value || "0", 10))
                          )
                        }
                      />
                    </div>
                    <div className="emergencyRow">
                      <div className="emergencyMeta">
                        <span className="label">Passenger Medical Emergencies</span>
                        <span className="helperText">
                          Prioritised above standard inbound traffic
                        </span>
                      </div>
                      <input
                        className="input"
                        type="number"
                        min={0}
                        value={medicalEmergencyCount}
                        onChange={(e) =>
                          setMedicalEmergencyCount(
                            Math.max(0, parseInt(e.target.value || "0", 10))
                          )
                        }
                      />
                    </div>
                    <div className="emergencyRow">
                      <div className="emergencyMeta">
                        <span className="label">Technical Emergencies</span>
                        <span className="helperText">Handled as elevated priority</span>
                      </div>
                      <input
                        className="input"
                        type="number"
                        min={0}
                        value={technicalEmergencyCount}
                        onChange={(e) =>
                          setTechnicalEmergencyCount(
                            Math.max(0, parseInt(e.target.value || "0", 10))
                          )
                        }
                      />
                    </div>
                  </div>
                  {emergencyValidationMessage ? (
                    <div className="validationText">{emergencyValidationMessage}</div>
                  ) : null}
                </SectionCard>
              </section>

              <section className="section">
                <SectionCard
                  title="Simulation Summary"
                  subtitle="Live overview of the current simulation setup"
                  icon="📊"
                >
                  <SummaryCard
                    inboundRate={inboundRate}
                    outboundRate={outboundRate}
                    runwayCount={runwayCount}
                    runways={runways}
                    emergencyPriority={emergencyPriority}
                    cancelAfterMins={cancelAfterMins}
                    fuelEmergencyCount={fuelEmergencyCount}
                    medicalEmergencyCount={medicalEmergencyCount}
                    technicalEmergencyCount={technicalEmergencyCount}
                  />
                </SectionCard>
              </section>
            </div>

            <div className="configRight">
              <section className="section">
                <SectionCard
                  title="Runway Configuration"
                  subtitle="Configure runway count, mode, and operational status"
                  icon="🛬"
                >
                  <div className="row">
                    <label className="label">Number of Runways:</label>
                    <select
                      className="select"
                      value={runwayCount}
                      onChange={(e) => setRunwayCount(parseInt(e.target.value, 10))}
                    >
                      {Array.from({ length: 10 }, (_, i) => i + 1).map((n) => (
                        <option key={n} value={n}>
                          {n}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="divider" />

                  <div className="runwayList">
                    {runways.map((r) => (
                      <div className="runwayRow" key={r.id}>
                        <div className="runwayLeft">
                          <span className="runwayLabel">Runway {r.id}:</span>
                          <span className="runwayModeIcon" aria-hidden="true">
                            {r.status === "Closed" ? "⛔" : runwayModeIcon(r.mode)}
                          </span>
                          <select
                            className="select"
                            value={r.mode}
                            onChange={(e) => updateRunway(r.id, { mode: e.target.value })}
                          >
                            {RUNWAY_MODES.map((m) => (
                              <option key={m} value={m}>
                                {m}
                              </option>
                            ))}
                          </select>
                        </div>

                        <div className="runwayRight">
                          <Segmented
                            value={r.status}
                            options={RUNWAY_STATUSES}
                            onChange={(v) => updateRunway(r.id, { status: v })}
                            accent={r.status === "Enabled" ? "green" : "red"}
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                </SectionCard>
              </section>
            </div>
          </div>
        </div>
      </div>

      <footer className="bottombar">
        <button className="btn btnReset" onClick={onReset}>
          Reset
        </button>
        <button
          className={`btn btnPrimary wide ${isStarting ? "btnStarting" : ""}`}
          onClick={handleRunSimulation}
          disabled={isStarting || hasEmergencyValidationError}
        >
          {isStarting ? "Starting simulation..." : "Run Simulation"}
        </button>
        {hasEmergencyValidationError ? (
          <span className="bottombarNote">
            Fix emergency totals to enable simulation.
          </span>
        ) : null}
        <button className="btn btnExit" onClick={() => navigate("/")}>
          Exit
        </button>
      </footer>

      <HistoryPanel
        isOpen={historyOpen}
        onClose={() => setHistoryOpen(false)}
        entries={historyEntries}
        onSelect={handleHistorySelect}
      />

      <HelpPanel isOpen={helpOpen} onClose={() => setHelpOpen(false)} />
    </div>
  );
}

function runwayModeIcon(mode) {
  if (mode === "Landing") return "🛬";
  if (mode === "Take-off") return "🛫";
  return "↔";
}

function SliderRow({ label, value, unit, min, max, step, onChange }) {
  return (
    <div className="sliderRow">
      <div className="sliderTop">
        <span className="label">{label}:</span>
        <span className="valueText">
          <b>{value}</b> <span className="muted">{unit}</span>
        </span>
      </div>
      <input
        className="slider"
        type="range"
        min={min}
        max={max}
        step={step}
        value={value}
        onChange={(e) => onChange(parseInt(e.target.value, 10))}
      />
    </div>
  );
}

function Segmented({ value, options, onChange, accent = "gray" }) {
  return (
    <div className={`segmented segmented-${accent}`}>
      {options.map((opt) => (
        <button
          key={opt}
          className={`segBtn ${opt === value ? "active" : ""}`}
          type="button"
          onClick={() => onChange(opt)}
        >
          {opt}
        </button>
      ))}
    </div>
  );
}
