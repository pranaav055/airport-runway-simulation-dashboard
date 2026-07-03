package com.pranaav.airportsim.domain;

/**
 * Represents the aggregated output of a completed simulation run.
 *
 * This record stores summary performance metrics describing how the airport
 * operated during the simulation period. These values are calculated by the
 * simulation engine and returned to the frontend through the API.
 *
 * Metrics include queue sizes, average waiting times, delays relative to
 * scheduled operations, and disruption counts such as cancellations and
 * diversions.
 *
 * The record is immutable and acts as a Data Transfer Object (DTO) used to
 * serialise simulation results into JSON for client applications.
 */
public record SimulationResult(

        // Departure metrics
        int maxTakeoffQueueSize,
        double avgTakeoffQueueWaitMinutes,
        long maxDepartureDelayMinutes,
        double avgDepartureDelayMinutes,

        // Arrival Metrics
        int maxHoldingPatternSize,
        double avgHoldingTimeMinutes,
        long maxArrivalDelayMinutes,
        double avgArrivalDelayMinutes,

        // Disruption Metrics
        int cancelledCount,
        int divertedCount
) {
    // Explicit getters retained for compatibility with existing code or frameworks
    public int getMaxTakeoffQueueSize() { return maxTakeoffQueueSize; }
    public double getAvgTakeoffQueueWaitMinutes() { return avgTakeoffQueueWaitMinutes; }
    public long getMaxDepartureDelayMinutes() { return maxDepartureDelayMinutes; }
    public double getAvgDepartureDelayMinutes() { return avgDepartureDelayMinutes; }

    public int getMaxHoldingPatternSize() { return maxHoldingPatternSize; }
    public double getAvgHoldingTimeMinutes() { return avgHoldingTimeMinutes; }
    public long getMaxArrivalDelayMinutes() { return maxArrivalDelayMinutes; }
    public double getAvgArrivalDelayMinutes() { return avgArrivalDelayMinutes; }

    public int getCancelledCount() { return cancelledCount; }
    public int getDivertedCount() { return divertedCount; }
}