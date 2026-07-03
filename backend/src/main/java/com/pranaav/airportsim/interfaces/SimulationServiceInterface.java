package com.pranaav.airportsim.interfaces;

import com.pranaav.airportsim.domain.ScenarioConfig;
import com.pranaav.airportsim.domain.SimulationResult;

/**
 * Defines the contract for executing the airport simulation.
 *
 * Implementations of this interface are responsible for running the
 * simulation using the provided scenario configuration and returning
 * the resulting performance metrics.
 *
 * This abstraction allows the simulation logic to remain decoupled
 * from the API/controller layer, making it easier to modify or replace
 * the simulation implementation without affecting other parts of the system.
 */
public interface SimulationServiceInterface {
    /**
     * Executes a simulation run using the provided configuration.
     *
     * @param config the configuration parameters defining the simulation scenario
     * @return aggregated metrics describing the simulation outcome
     */
    SimulationResult calculateSimulation(ScenarioConfig config);
}