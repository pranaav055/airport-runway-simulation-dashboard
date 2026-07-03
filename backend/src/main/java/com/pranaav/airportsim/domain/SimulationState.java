package com.pranaav.airportsim.domain;
import com.pranaav.airportsim.enums.RunwayOperatingMode;

import java.util.*;

/**
 * Stores all mutable state used during a simulation run.
 *
 * This class acts as the central data container shared by all simulation
 * events. It holds the aircraft queues, runway information, configuration
 * parameters, random generator, and performance metrics collected during
 * the simulation.
 *
 * Simulation events operate on this state object to update queues, assign
 * runways, record metrics, and track disruptions.
 */
public class SimulationState {

    private final ArrayDeque<Aircraft> takeOffQueue;
    private final PriorityQueue<Aircraft> holdingPattern;

    private final Map<Long, List<Aircraft>> inboundArrivalsAt;
    private final Map<Long, List<Aircraft>> outboundReadyAt;

    private final ArrayList<Runway> runways;
    private final Random rng;
    private final long cancellationThresholdMinutes;
    private final long duration;

    // Departure metrics
    private int maxTakeOffQueueSize = 0;
    private long sumTakeOffWait = 0;
    private int numTakeOffs = 0;
    private long sumDepartureDelay = 0;
    private long maxDepartureDelay = 0;

    // Arrival metrics
    private int maxHoldingSize = 0;
    private long sumHoldingTime = 0;
    private int numLandings = 0;
    private long sumArrivalDelay = 0;
    private long maxArrivalDelay = 0;

    // Disruptions
    private int cancelledCount = 0;
    private int divertedCount = 0;

    /**
     * Creates the simulation state with initial queues, schedules,
     * runways, and configuration parameters.
     */
    public SimulationState(
            ArrayDeque<Aircraft> takeOffQueue,
            PriorityQueue<Aircraft> holdingPattern,
            Map<Long, List<Aircraft>> inboundArrivalsAt,
            Map<Long, List<Aircraft>> outboundReadyAt,
            ArrayList<Runway> runways,
            Random rng,
            long cancellationThresholdMinutes,
            long duration
    ) {
        this.takeOffQueue = takeOffQueue;
        this.holdingPattern = holdingPattern;
        this.inboundArrivalsAt = inboundArrivalsAt;
        this.outboundReadyAt = outboundReadyAt;
        this.runways = runways;
        this.rng = rng;
        this.cancellationThresholdMinutes = cancellationThresholdMinutes;
        this.duration = duration;
    }

    /**
     * Adds an aircraft to the take-off queue and updates queue metrics.
     */
    public void addToTakeOffQueue(Aircraft aircraft) {
        takeOffQueue.add(aircraft);
        updateMaxTakeOffQueueSize();
    }

    public ArrayDeque<Aircraft> getTakeOffQueue() {
        return takeOffQueue;
    }

    public PriorityQueue<Aircraft> getHoldingPattern() {
        return holdingPattern;
    }

    /**
     * Updates the maximum take-off queue size observed during the simulation.
     */
    public void updateMaxTakeOffQueueSize() {
        maxTakeOffQueueSize = Math.max(maxTakeOffQueueSize, takeOffQueue.size());
    }

    /**
     * Removes and returns the next aircraft from the take-off queue.
     */
    public Aircraft pollTakeOffQueue() {
        if (isTakeOffQueueEmpty())
            return null;
        return takeOffQueue.pollFirst();
    }

    /**
     * Removes and returns the next aircraft from the holding pattern.
     */
    public Aircraft pollHoldingPattern() {
        if (isHoldingPatternEmpty())
            return null;
        return holdingPattern.poll();
    }

    /**
     * Adds an aircraft to the holding pattern and updates holding metrics.
     */
    public void addToHoldingPattern(Aircraft aircraft) {
        holdingPattern.add(aircraft);
        updateMaxHoldingSize();
    }

    public long getDuration() {
        return duration;
    }

    /**
     * Returns inbound aircraft scheduled to enter the system at a given minute.
     */
    public List<Aircraft> getInboundArrivalsAt(long now) {
        return inboundArrivalsAt.getOrDefault(now, new ArrayList<>());
    }

    /**
     * Returns outbound aircraft scheduled to become ready at a given minute.
     */
    public List<Aircraft> getOutboundReadyAt(long now) {
        return outboundReadyAt.getOrDefault(now, new ArrayList<>());
    }

    /**
     * Returns runways available for take-off operations at the current time.
     */
    public ArrayList<Runway> getTakeOffRunwaysAvailableAt(long now) {
        ArrayList<Runway> takeOffRunways = new ArrayList<>();
        for (Runway runway : runways) {
            RunwayOperatingMode mode = runway.getOperatingMode();
            if ((mode == RunwayOperatingMode.TAKEOFF || mode == RunwayOperatingMode.MIXED) && runway.isAvailableAt(now)) {
                takeOffRunways.add(runway);
            }
        }
        return takeOffRunways;
    }

    /**
     * Returns runways available for landing operations at the current time.
     */
    public ArrayList<Runway> getLandingRunwaysAvailableAt(long now) {
        ArrayList<Runway> landingRunways = new ArrayList<>();
        for (Runway runway : runways) {
            RunwayOperatingMode mode = runway.getOperatingMode();
            if ((mode == RunwayOperatingMode.LANDING || mode == RunwayOperatingMode.MIXED) && runway.isAvailableAt(now))
                landingRunways.add(runway);
        }
        return landingRunways;
    }

    /**
     * Removes an aircraft from the take-off queue (used when cancelled).
     */
    public void removeFromTakeOffQueue(Aircraft aircraft) {
        if (isTakeOffQueueEmpty())
            return;
        takeOffQueue.remove(aircraft);
    }

    /**
     * Removes an aircraft from the holding pattern (used when diverted).
     */
    public void removeFromHoldingPattern(Aircraft aircraft) {
        if (isHoldingPatternEmpty())
            return;
        holdingPattern.remove(aircraft);
    }

    public Random getRng() {
        return rng;
    }

    public long getCancellationThresholdMinutes() {
        return cancellationThresholdMinutes;
    }

    public int getMaxTakeOffQueueSize() {
        return maxTakeOffQueueSize;
    }

    /**
     * Records metrics for a completed take-off operation.
     */
    public void recordTakeOff(long wait, long delay) {
        numTakeOffs++;
        sumTakeOffWait += wait;
        sumDepartureDelay += delay;
        maxDepartureDelay = Math.max(maxDepartureDelay, delay);
    }

    public int getMaxHoldingSize() {
        return maxHoldingSize;
    }

    public boolean isHoldingPatternEmpty() {
        return holdingPattern.isEmpty();
    }

    public boolean isTakeOffQueueEmpty() {
        return takeOffQueue.isEmpty();
    }

    /**
     * Updates the maximum holding pattern size observed during the simulation.
     */
    public void updateMaxHoldingSize() {
        maxHoldingSize = Math.max(maxHoldingSize, holdingPattern.size());
    }

    /**
     * Records metrics for a completed landing operation.
     */
    public void recordLanding(long holdingTime, long delay) {
        numLandings++;
        sumHoldingTime += holdingTime;
        sumArrivalDelay += delay;
        maxArrivalDelay = Math.max(maxArrivalDelay, delay);
    }

    public void incrementCancelledCount() {
        cancelledCount++;
    }

    public void incrementDivertedCount() {
        divertedCount++;
    }

    /**
     * Converts collected simulation metrics into an immutable SimulationResult.
     */
    public SimulationResult toSimulationResult() {
        double avgTakeOffWait = (numTakeOffs == 0) ? 0.0 : (double) sumTakeOffWait / numTakeOffs;
        double avgDepartureDelay = (numTakeOffs == 0) ? 0.0 : (double) sumDepartureDelay / numTakeOffs;

        double avgHoldingTime = (numLandings == 0) ? 0.0 : (double) sumHoldingTime / numLandings;
        double avgArrivalDelay = (numLandings == 0) ? 0.0 : (double) sumArrivalDelay / numLandings;

        return new SimulationResult(
                maxTakeOffQueueSize,
                avgTakeOffWait,
                maxDepartureDelay,
                avgDepartureDelay,
                maxHoldingSize,
                avgHoldingTime,
                maxArrivalDelay,
                avgArrivalDelay,
                cancelledCount,
                divertedCount
        );
    }
}