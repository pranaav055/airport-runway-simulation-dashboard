package com.pranaav.airportsim.domain;

import java.util.List;

/**
 * Represents an extended simulation output used for debugging or analysis.
 *
 * In addition to the standard aggregated metrics returned by SimulationResult,
 * this class exposes internal simulation data such as the list of aircraft
 * involved in the simulation and a timeline of runway usage events.
 *
 * This allows developers or visualisation tools to inspect how the simulation
 * progressed step-by-step, rather than only viewing summary statistics.
 */
public class SimulationDebugResult {

    private final SimulationResult result;    // Aggregated performance metrics from the simulation
    private final List<Aircraft> aircraft;    // All aircraft generated and processed during the simulation
    private final List<RunwayUseEvent> runwayUses;    // Timeline of runway usage events recorded during the simulation

    /**
     * Creates a debug result containing both summary metrics and detailed
     * simulation state information.
     *
     * @param result aggregated simulation metrics
     * @param aircraft list of aircraft involved in the simulation
     * @param runwayUses recorded runway usage events
     */
    public SimulationDebugResult(SimulationResult result, List<Aircraft> aircraft, List<RunwayUseEvent> runwayUses) {
        this.result = result;
        this.aircraft = aircraft;
        this.runwayUses = runwayUses;
    }

    public SimulationResult getResult() { return result; }
    public List<Aircraft> getAircraft() { return aircraft; }
    public List<RunwayUseEvent> getRunwayUses() { return runwayUses; }
}