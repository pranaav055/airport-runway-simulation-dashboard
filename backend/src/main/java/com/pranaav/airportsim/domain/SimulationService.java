package com.pranaav.airportsim.domain;

import com.pranaav.airportsim.enums.EmergencyStatus;
import com.pranaav.airportsim.interfaces.SimulationEventInterface;
import com.pranaav.airportsim.interfaces.SimulationServiceInterface;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

/**
 * Minute-by-minute simulation (0..durationMinutes).
 *
 * Spec-aligned ideas used here:
 *  - Aircraft have a scheduled time (arrival/departure).
 *  - Their queue-entry time is jittered by Normal(0, 5 minutes) around the scheduled time.
 *  - Outbound aircraft join a FIFO take-off queue.
 *  - Inbound aircraft join a holding pattern with priority (emergency first, then FIFO).
 *  - Runways have modes: LANDING / TAKEOFF / MIXED and an operational status.
 *  - Runway exclusivity: a runway can only serve one aircraft at a time.
 *  - Outbound cancellation if waiting time exceeds threshold.
 *  - Inbound diversion if holding time reaches fuel limit.
 *
 * Modelling assumption:
 *  - Inbound aircraft are given a random fuel allowance uniformly in [20, 60] minutes.
 */
@Service
public class SimulationService implements SimulationServiceInterface {

    private static final double SCHEDULE_JITTER_SIGMA_MIN = 5.0;
    private static final double MIN_INBOUND_FUEL_MIN = 20.0;
    private static final double MAX_INBOUND_FUEL_MIN = 60.0;

    /**
     * Executes a full simulation run using the provided scenario configuration.
     *
     * This method performs the complete simulation workflow:
     *  - Reads the configuration
     *  - Generates inbound and outbound aircraft schedules
     *  - Creates the initial simulation state
     *  - Builds the ordered event pipeline
     *  - Executes the minute-by-minute loop
     *  - Returns the final aggregated metrics
     *
     * @param config simulation configuration
     * @return aggregated result of the simulation run
     */
    @Override
    public SimulationResult calculateSimulation(ScenarioConfig config) {
        long duration = config.getDurationMinutes();
        long cancellationThresholdMinutes = config.getCancellationThresholdMinutes();
        double inboundFlowPerHour = config.getInboundFlowPerHour();
        double outboundFlowPerHour = config.getOutboundFlowPerHour();
        ArrayList<Runway> runways = config.getRunways();

        Random rng = new Random(config.getRandomSeed());

        Map<Long, List<Aircraft>> inboundArrivalsAt =
                generateInboundArrivals(duration, inboundFlowPerHour, rng);

        Map<Long, List<Aircraft>> outboundReadyAt =
                generateOutboundReady(duration, outboundFlowPerHour, rng);

        SimulationState state = createInitialState(
                inboundArrivalsAt,
                outboundReadyAt,
                runways,
                rng,
                cancellationThresholdMinutes,
                duration
        );

        List<SimulationEventInterface> events = buildEventPipeline();

        runSimulationLoop(state, events, duration);

        return state.toSimulationResult();
    }

    /**
     * Generates inbound aircraft arrivals for the simulation.
     *
     * Aircraft are created according to the configured inbound flow rate,
     * assigned scheduled times, jittered entry times, and random initial fuel.
     *
     * @param duration simulation duration in minutes
     * @param inboundFlowPerHour average number of inbound aircraft per hour
     * @param rng random generator for stochastic sampling
     * @return map from simulation minute to list of inbound aircraft entering at that time
     */
    private Map<Long, List<Aircraft>> generateInboundArrivals(long duration,
                                                              double inboundFlowPerHour,
                                                              Random rng) {
        Map<Long, List<Aircraft>> inboundArrivalsAt = new HashMap<>();

        if (inboundFlowPerHour <= 0) {
            return inboundArrivalsAt;
        }

        double inboundInterval = 60.0 / inboundFlowPerHour;
        int id = 0;

        for (double sched = 0.0; sched <= duration; sched += inboundInterval) {
            long scheduledTime = Math.round(sched);
            long entryTime = jitteredEntryTime(scheduledTime, duration, rng);
            double fuelMinutes = sampleInboundFuelMinutes(rng);

            Aircraft aircraft = Aircraft.createInbound(
                    "IN" + id++,
                    "OP",
                    "AAA",
                    "BBB",
                    scheduledTime,
                    EmergencyStatus.NONE,
                    fuelMinutes
            );

            inboundArrivalsAt
                    .computeIfAbsent(entryTime, k -> new ArrayList<>())
                    .add(aircraft);
        }

        return inboundArrivalsAt;
    }

    /**
     * Generates outbound aircraft ready times for the simulation.
     *
     * Aircraft are created according to the configured outbound flow rate,
     * assigned scheduled times, and given a jittered queue-entry time.
     *
     * @param duration simulation duration in minutes
     * @param outboundFlowPerHour average number of outbound aircraft per hour
     * @param rng random generator for stochastic sampling
     * @return map from simulation minute to list of outbound aircraft ready at that time
     */
    private Map<Long, List<Aircraft>> generateOutboundReady(long duration,
                                                            double outboundFlowPerHour,
                                                            Random rng) {
        Map<Long, List<Aircraft>> outboundReadyAt = new HashMap<>();

        if (outboundFlowPerHour <= 0) {
            return outboundReadyAt;
        }

        double outboundInterval = 60.0 / outboundFlowPerHour;
        int id = 0;

        for (double sched = 0.0; sched <= duration; sched += outboundInterval) {
            long scheduledTime = Math.round(sched);
            long entryTime = jitteredEntryTime(scheduledTime, duration, rng);

            Aircraft aircraft = Aircraft.createOutbound(
                    "OUT" + id++,
                    "OP",
                    "CCC",
                    "DDD",
                    scheduledTime,
                    EmergencyStatus.NONE
            );

            outboundReadyAt
                    .computeIfAbsent(entryTime, k -> new ArrayList<>())
                    .add(aircraft);
        }

        return outboundReadyAt;
    }

    /**
     * Creates the initial mutable simulation state before execution begins.
     *
     * This includes queue structures, pre-generated schedules, runway list,
     * random generator, and key scenario parameters.
     *
     * @param inboundArrivalsAt pre-generated inbound arrival schedule
     * @param outboundReadyAt pre-generated outbound ready schedule
     * @param runways configured runways for the scenario
     * @param rng seeded random generator
     * @param cancellationThresholdMinutes threshold before outbound cancellation
     * @param duration simulation duration
     * @return initialised simulation state
     */
    private SimulationState createInitialState(Map<Long, List<Aircraft>> inboundArrivalsAt,
                                               Map<Long, List<Aircraft>> outboundReadyAt,
                                               ArrayList<Runway> runways,
                                               Random rng,
                                               long cancellationThresholdMinutes,
                                               long duration) {
        return new SimulationState(
                new ArrayDeque<>(),
                new PriorityQueue<>(new AircraftComparator()),
                inboundArrivalsAt,
                outboundReadyAt,
                runways,
                rng,
                cancellationThresholdMinutes,
                duration
        );
    }

    /**
     * Builds the ordered event pipeline executed during each simulation minute.
     *
     * The order is important because it defines how the simulation evolves:
     * arrivals and departures enter the system first, runway allocation occurs next,
     * and disruption checks are then applied.
     *
     * @return ordered list of simulation events
     */
    private List<SimulationEventInterface> buildEventPipeline() {
        return List.of(
                new SimulationEvents.InboundArrivalEvent(),
                new SimulationEvents.OutboundReadyEvent(),
                new SimulationEvents.LandingEvent(),
                new SimulationEvents.TakeOffEvent(),
                new SimulationEvents.DiversionEvent(),
                new SimulationEvents.CancellationEvent()
            );
    }

    /**
     * Runs the minute-by-minute simulation loop.
     *
     * For each simulation minute, every event in the pipeline is applied
     * to the shared simulation state.
     *
     * @param state mutable simulation state
     * @param events ordered list of events
     * @param duration total simulation duration in minutes
     */
    private void runSimulationLoop(SimulationState state,
                                   List<SimulationEventInterface> events,
                                   long duration) {
        for (long now = 0; now <= duration; now++) {
            for (SimulationEventInterface event : events) {
                event.process(now, state);
            }
        }
    }

    /**
     * Generates a queue-entry time by applying Gaussian jitter
     * around the scheduled time and clamping the result to the
     * simulation bounds.
     *
     * @param scheduledTime original scheduled time
     * @param duration simulation duration
     * @param rng random generator
     * @return bounded jittered time
     */
    private long jitteredEntryTime(long scheduledTime, long duration, Random rng) {
        double jitter = rng.nextGaussian() * SCHEDULE_JITTER_SIGMA_MIN;
        return clamp(Math.round(scheduledTime + jitter), 0, duration);
    }

    /**
     * Samples an initial inbound fuel allowance uniformly
     * between the configured minimum and maximum bounds.
     *
     * @param rng random generator
     * @return fuel allowance in minutes
     */
    private double sampleInboundFuelMinutes(Random rng) {
        return MIN_INBOUND_FUEL_MIN
                + ((MAX_INBOUND_FUEL_MIN - MIN_INBOUND_FUEL_MIN) * rng.nextDouble());
    }

    /**
     * Restricts a value to lie within a given range.
     *
     * @param value value to clamp
     * @param min lower bound
     * @param max upper bound
     * @return bounded value
     */
    private long clamp(long value, long min, long max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Convenience wrapper for running the simulation directly.
     *
     * @param config simulation configuration
     * @return aggregated simulation result
     */
    public SimulationResult run(ScenarioConfig config) {
        return calculateSimulation(config);
    }

    /**
     * Placeholder for a future debug-oriented simulation mode.
     *
     * Intended to return extended state information such as aircraft traces
     * and runway usage events in addition to the normal result metrics.
     *
     * @param config simulation configuration
     * @return extended debug result
     */
    public SimulationDebugResult runDebug(ScenarioConfig config) {
        throw new UnsupportedOperationException("runDebug not implemented yet");
    }
}
