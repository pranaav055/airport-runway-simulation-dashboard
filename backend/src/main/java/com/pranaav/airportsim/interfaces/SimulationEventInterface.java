package com.pranaav.airportsim.interfaces;

import com.pranaav.airportsim.domain.SimulationState;

/**
 * Represents a simulation event that can be executed during a simulation step.
 *
 * Implementations of this interface define specific behaviours that occur
 * during the simulation, such as aircraft arrivals, departures, landings,
 * cancellations, or diversions.
 *
 * Each event operates on the shared {@link SimulationState}, allowing it to
 * inspect and modify the current simulation data.
 */
public interface SimulationEventInterface {
    /**
     * Executes the event logic for the current simulation minute.
     *
     * @param now the current simulation time (in minutes since simulation start)
     * @param state the shared simulation state containing queues, runways,
     *              and metrics that may be updated by the event
     */
    void process(long now, SimulationState state);
}