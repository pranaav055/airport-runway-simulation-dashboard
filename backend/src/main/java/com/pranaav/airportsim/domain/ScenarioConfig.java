package com.pranaav.airportsim.domain;

import java.util.*;

/**
 * Represents the configuration settings for a simulation scenario.
 *
 * This class encapsulates all parameters required to initialise and run
 * a simulation, including simulation duration, aircraft traffic flow
 * rates, cancellation rules, randomisation seed, and the runways
 * available at the airport.
 *
 * A ScenarioConfig object is created before the simulation begins and
 * remains immutable throughout execution, ensuring consistent
 * configuration behaviour.
 */
public class ScenarioConfig {

    private final long durationMinutes;          // Total simulation duration in minutes
    private final double inboundFlowPerHour;     // Average number of inbound aircraft generated per hour
    private final double outboundFlowPerHour;    // Average number of outbound aircraft generated per hour
    private final long randomSeed;               // Seed used to initialise the simulation's random generator
    
    private final long cancellationThresholdMinutes; // Max time an aircraft can be delayed before cancelled/diverted
    private final ArrayList<Runway> runways;         // List of runways available for use in the simulation (1-10 runways)

    /**
     * Constructs a new ScenarioConfig with specified simulation parameters.
     *
     * @param durationMinutes total simulation runtime in minutes
     * @param inboundFlowPerHour inbound aircraft generation rate per hour
     * @param outboundFlowPerHour outbound aircraft generation rate per hour
     * @param cancellationThresholdMinutes delay threshold before cancellations occur
     * @param randomSeed seed used to control deterministic randomness
     * @param runways list of runways available in the scenario
     */
    public ScenarioConfig(
            long durationMinutes,
            double inboundFlowPerHour,
            double outboundFlowPerHour,
            long cancellationThresholdMinutes,
            long randomSeed,
            List<Runway> runways
    ) {
        // Validate input parameters to ensure the scenario configuration is valid
        if (durationMinutes <= 0) throw new IllegalArgumentException("durationMinutes must be > 0");
        if (inboundFlowPerHour < 0) throw new IllegalArgumentException("inboundFlowPerHour must be >= 0");
        if (outboundFlowPerHour < 0) throw new IllegalArgumentException("outboundFlowPerHour must be >= 0");
        if (cancellationThresholdMinutes <= 0) throw new IllegalArgumentException("cancellationThresholdMinutes must be > 0");
        if (runways == null || runways.isEmpty()) throw new IllegalArgumentException("runways cannot be null/empty");
        if (runways.size() < 1 || runways.size() > 10) throw new IllegalArgumentException("runways must be between 1 and 10");
    
        this.durationMinutes = durationMinutes;
        this.inboundFlowPerHour = inboundFlowPerHour;
        this.outboundFlowPerHour = outboundFlowPerHour;
        this.cancellationThresholdMinutes = cancellationThresholdMinutes;
        this.randomSeed = randomSeed;

        // Copy runway list to prevent external modification after creation
        this.runways = new ArrayList<>(runways);
    }

    // Returns the total simulation duration in minutes
    public long getDurationMinutes() { return durationMinutes; }

    // Returns the inbound aircraft flow rate (per hour)
    public double getInboundFlowPerHour() { return inboundFlowPerHour; }

    // Returns the outbound aircraft flow rate (per hour)
    public double getOutboundFlowPerHour() { return outboundFlowPerHour; }

    // Returns the cancellation threshold for delayed aircrafts
    public long getCancellationThresholdMinutes() { return cancellationThresholdMinutes; }

    // Returns the random seed used for simulation generation
    public long getRandomSeed() { return randomSeed; }

    // Returns the list of runways used in the simulation
    public ArrayList<Runway> getRunways() { return runways; }
}