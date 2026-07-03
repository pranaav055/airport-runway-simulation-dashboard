package com.pranaav.airportsim.domain;

import com.pranaav.airportsim.enums.AircraftStatus;
import com.pranaav.airportsim.interfaces.SimulationEventInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Contains the concrete simulation events used by the event-driven
 * airport simulation engine.
 *
 * Each nested class implements {@link SimulationEventInterface} and
 * represents a specific type of state transition that may occur during
 * a simulation minute, such as aircraft arrivals, departures, landings,
 * cancellations, or diversions.
 *
 * These events operate on a shared {@link SimulationState} object,
 * which stores the queues, runways, metrics, and random generator
 * required for simulation execution.
 */
public class SimulationEvents {

    // Runway service-time assumption: mean 13.5, sd 1.0 (clamped >= 1)
    private static final double RUNWAY_SERVICE_MEAN_MIN = 13.5;
    private static final double RUNWAY_SERVICE_SD_MIN = 1.0;
    private static final long MIN_SERVICE_TIME_MIN = 1;

    /**
     * Handles inbound aircraft entering the airport airspace.
     *
     * Aircraft scheduled to arrive at the current minute are added
     * to the holding pattern and marked as queued.
     */
    public static class InboundArrivalEvent implements SimulationEventInterface {
        @Override
        public void process(long now, SimulationState state) {
            List<Aircraft> readyArrivals = state.getInboundArrivalsAt(now);
            for (Aircraft aircraft : readyArrivals) {
                // Add to holding pattern and update max holding pattern size
                state.addToHoldingPattern(aircraft);
                aircraft.setStatus(AircraftStatus.QUEUED);
                aircraft.setQueueEntryTimeMinutes(now);
            }
        }
    }

    /**
     * Handles outbound aircraft becoming ready for departure.
     *
     * Aircraft scheduled to depart at the current minute are added
     * to the FIFO take-off queue and marked as queued.
     */
    public static class OutboundReadyEvent implements SimulationEventInterface {
        @Override
        public void process(long now, SimulationState state) {
            List<Aircraft> readyDepartures = state.getOutboundReadyAt(now);
            for (Aircraft aircraft : readyDepartures) {
                // Add to takeoff queue and update max takeoff queue size
                state.addToTakeOffQueue(aircraft);
                aircraft.setStatus(AircraftStatus.QUEUED);
                aircraft.setQueueEntryTimeMinutes(now);
            }
        }
    }

    /**
     * Handles take-off allocation for outbound aircraft.
     *
     * Available take-off runways are assigned to aircraft waiting
     * in the take-off queue. Once assigned, the aircraft is marked
     * as served and departure metrics are recorded.
     */
    public static class TakeOffEvent implements SimulationEventInterface {

        @Override
        public void process(long now, SimulationState state) {

            ArrayList<Runway> takeOffRunways = state.getTakeOffRunwaysAvailableAt(now);
            for (Runway runway : takeOffRunways) {

                // Stop assigning runways if queue becomes empty
                if (state.isTakeOffQueueEmpty())
                    break;

                Aircraft aircraft = state.pollTakeOffQueue();
                // Process the takeoff event for the aircraft
                long service = sampleRunwayServiceTimeMinutes(state.getRng());
                long actual = clamp(now + service, now, state.getDuration());
                runway.occupyUntil(actual);
                aircraft.setActualTimeMinutes(actual);
                aircraft.setStatus(AircraftStatus.SERVED);

                long entry = aircraft.getQueueEntryTimeMinutes() == null ? now : aircraft.getQueueEntryTimeMinutes();
                long wait  = actual - entry;
                long delay = actual - aircraft.getScheduledTimeMinutes();

                state.recordTakeOff(wait, delay);
            }
        }
    }

    /**
     * Handles landing allocation for inbound aircraft.
     *
     * Available landing runways are assigned to aircraft waiting
     * in the holding pattern. Once assigned, the aircraft is marked
     * as served and arrival metrics are recorded.
     */
    public static class LandingEvent implements SimulationEventInterface {
        
        @Override 
        public void process(long now, SimulationState state) {

            ArrayList<Runway> landingRunways = state.getLandingRunwaysAvailableAt(now);
            for (Runway runway : landingRunways) {

                // Stop assigning runways if queue becomes empty
                if (state.isHoldingPatternEmpty())
                    break;


                Aircraft aircraft = state.pollHoldingPattern();
                // Process the landing event for the aircraft
                long service = sampleRunwayServiceTimeMinutes(state.getRng());
                long actual = clamp(now + service, now, state.getDuration());

                runway.occupyUntil(actual);
                aircraft.setActualTimeMinutes(actual);
                aircraft.setStatus(AircraftStatus.SERVED);

                long entry = aircraft.getQueueEntryTimeMinutes() == null ? now : aircraft.getQueueEntryTimeMinutes();
                long holding = actual - entry;
                long delay   = actual - aircraft.getScheduledTimeMinutes();

                state.recordLanding(holding, delay);
            }
        }
    } 

    /**
     * Handles cancellation of outbound aircraft.
     *
     * Any aircraft that has remained in the take-off queue longer
     * than the configured cancellation threshold is removed and
     * marked as cancelled.
     */
    public static class CancellationEvent implements SimulationEventInterface {

        @Override
        public void process(long now, SimulationState state) {
            ArrayList<Aircraft> cancelledAircraft = new ArrayList<>();
            for (Aircraft aircraft : state.getTakeOffQueue()) {
                // Waiting for too long
                if (now - aircraft.getQueueEntryTimeMinutes() > state.getCancellationThresholdMinutes()) {
                    aircraft.setStatus(AircraftStatus.CANCELLED);
                    cancelledAircraft.add(aircraft);
                    state.incrementCancelledCount();
                }
            }
            for (Aircraft aircraft : cancelledAircraft) {
                state.removeFromTakeOffQueue(aircraft);
            }
        }
    }

    /**
     * Handles diversion of inbound aircraft.
     *
     * Aircraft remaining too long in the holding pattern may divert
     * once their remaining fuel would fall below the safe threshold.
     */
    public static class DiversionEvent implements SimulationEventInterface {
        
        @Override
        public void process(long now, SimulationState state) {
            ArrayList<Aircraft> divertedAircraft = new ArrayList<>();
            for (Aircraft aircraft : state.getHoldingPattern()) {
                double fuelMinutesUsed = now - aircraft.getQueueEntryTimeMinutes();
                if (aircraft.getFuelMinutesRemaining() - fuelMinutesUsed < 10) {
                    aircraft.setStatus(AircraftStatus.DIVERTED);
                    divertedAircraft.add(aircraft);
                    state.incrementDivertedCount();
                }
            }
            for (Aircraft aircraft : divertedAircraft) {
                state.removeFromHoldingPattern(aircraft);
            }
        }
    }

    /**
     * Samples a runway service time in minutes from Normal(mean=13.5, sd=1.0)
     * and clamps to at least 1 minute.
     * 
     * @param rng random number generator used for stochastic behaviour
     * @return sampled runway service time in minutes
     */
    private static long sampleRunwayServiceTimeMinutes(Random rng) {
        double sample = RUNWAY_SERVICE_MEAN_MIN + rng.nextGaussian() * RUNWAY_SERVICE_SD_MIN;
        long service = (long) Math.round(sample);
        return Math.max(MIN_SERVICE_TIME_MIN, service);
    }

    /**
     * Restricts a value to lie between a lower and upper bound.
     *
     * @param v value to clamp
     * @param lo lower bound
     * @param hi upper bound
     * @return bounded value
     */
    private static long clamp(long v, long lo, long hi) {
        return Math.max(lo, Math.min(hi, v));
    }
}