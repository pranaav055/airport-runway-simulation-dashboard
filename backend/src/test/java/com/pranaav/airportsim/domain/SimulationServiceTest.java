package com.pranaav.airportsim.domain;

import org.junit.jupiter.api.Test;

import com.pranaav.airportsim.enums.RunwayOperationalStatus;
import com.pranaav.airportsim.enums.RunwayOperatingMode;
import com.pranaav.airportsim.enums.AircraftStatus;
import com.pranaav.airportsim.enums.AircraftType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit and integration-style tests for the simulation engine.
 *
 * These tests verify:
 *  - Deterministic behaviour when using the same random seed (repeatability requirement).
 *  - Correct handling of cancellations and diversions under constrained runway modes.
 *  - Sanity of output metrics (non-negativity and expected zeros under zero traffic).
 *  - Input validation for ScenarioConfig (invalid parameters should throw exceptions).
 *
 * Note: Many tests use carefully chosen runway modes and flow rates to trigger
 * specific behaviours (e.g., cancellations when takeoff is impossible).
 */
class SimulationServiceTest{



    private void assertMetricsMatchAircraftList(SimulationResult r, List<Aircraft> aircraft) {

        // --- FR17 counts ---
        long cancelled = aircraft.stream().filter(a -> a.getStatus() == AircraftStatus.CANCELLED).count();
        long diverted  = aircraft.stream().filter(a -> a.getStatus() == AircraftStatus.DIVERTED).count();
        assertEquals((int) cancelled, r.getCancelledCount(), "Cancelled count should match aircraft statuses (FR17)");
        assertEquals((int) diverted,  r.getDivertedCount(),  "Diverted count should match aircraft statuses (FR17)");

        // --- FR04 departure metrics (computed over SERVED outbound aircraft, same as SimulationService) ---
        var outServed = aircraft.stream()
                .filter(a -> a.getAircraftType() == AircraftType.OUTBOUND)
                .filter(a -> a.getStatus() == AircraftStatus.SERVED)
                .filter(a -> a.getQueueEntryTimeMinutes() != null && a.getActualTimeMinutes() != null)
                .toList();

        long sumOutWait = 0;
        long sumOutDelay = 0;
        long maxOutDelay = 0;
        for (Aircraft a : outServed) {
            long wait = a.getActualTimeMinutes() - a.getQueueEntryTimeMinutes();
            long delay = a.getActualTimeMinutes() - a.getScheduledTimeMinutes();
            sumOutWait += wait;
            sumOutDelay += delay;
            maxOutDelay = Math.max(maxOutDelay, delay);
        }

        double avgOutWait  = outServed.isEmpty() ? 0.0 : (double) sumOutWait / outServed.size();
        double avgOutDelay = outServed.isEmpty() ? 0.0 : (double) sumOutDelay / outServed.size();

        assertEquals(avgOutWait,  r.getAvgTakeoffQueueWaitMinutes(), 1e-9, "Avg takeoff queue wait mismatch (FR04)");
        assertEquals(maxOutDelay, r.getMaxDepartureDelayMinutes(),         "Max departure delay mismatch (FR04)");
        assertEquals(avgOutDelay, r.getAvgDepartureDelayMinutes(), 1e-9,   "Avg departure delay mismatch (FR04)");

        // --- FR09 arrival metrics (computed over SERVED inbound aircraft, same as SimulationService) ---
        var inServed = aircraft.stream()
                .filter(a -> a.getAircraftType() == AircraftType.INBOUND)
                .filter(a -> a.getStatus() == AircraftStatus.SERVED)
                .filter(a -> a.getQueueEntryTimeMinutes() != null && a.getActualTimeMinutes() != null)
                .toList();

        long sumHold = 0;
        long sumArrDelay = 0;
        long maxArrDelay = 0;
        for (Aircraft a : inServed) {
            long holding = a.getActualTimeMinutes() - a.getQueueEntryTimeMinutes();
            long delay = a.getActualTimeMinutes() - a.getScheduledTimeMinutes();
            sumHold += holding;
            sumArrDelay += delay;
            maxArrDelay = Math.max(maxArrDelay, delay);
        }

        double avgHold     = inServed.isEmpty() ? 0.0 : (double) sumHold / inServed.size();
        double avgArrDelay = inServed.isEmpty() ? 0.0 : (double) sumArrDelay / inServed.size();

        assertEquals(avgHold,     r.getAvgHoldingTimeMinutes(), 1e-9, "Avg holding time mismatch (FR09)");
        assertEquals(maxArrDelay, r.getMaxArrivalDelayMinutes(),      "Max arrival delay mismatch (FR09)");
        assertEquals(avgArrDelay, r.getAvgArrivalDelayMinutes(), 1e-9,"Avg arrival delay mismatch (FR09)");
    }





    // ---------------------------------------------------
    // TC06 — FR11, FR13–FR16 (Multi-runway + disruptions)
    // ---------------------------------------------------
    
    /**
     * If the runway is configured such that takeoffs can never occur (LANDING only),
     * outbound aircraft should accumulate in the takeoff queue and eventually exceed
     * the cancellation threshold, triggering cancellations.
     */
    @Test
    void TC06_FR15_takeoffImpossible_outboundEventuallyCancels() {
        long seed = 1L;

        // LANDING-only runway means outbound aircraft cannot be served
        Runway landingOnly = new Runway("09",3500,90,RunwayOperatingMode.LANDING,RunwayOperationalStatus.AVAILABLE);
        
        // Outbound flow is high, cancellation threshold is short -> cancellations should occur
        ScenarioConfig config = new ScenarioConfig(120,0.0,60.0,5,seed,List.of(landingOnly));
        SimulationResult result = new SimulationService().run(config);

        assertTrue(result.getCancelledCount() > 0, "Expected at least one cancellation when takeoffs are impossible (FR15)");
    }

    /**
     * If landings are impossible (TAKEOFF only), inbound aircraft should remain in holding.
     * Since fuel decreases while holding, aircraft should eventually divert once fuel
     * drops below the minimum threshold.
     */
    @Test
    void TC06_FR16_landingImpossible_inboundEventuallyDiverts(){
        long seed = 1234L;

        // TAKEOFF-only runway means inbound aircraft can never land
        Runway takeoffOnly = new Runway("09", 3500, 90,RunwayOperatingMode.TAKEOFF,RunwayOperationalStatus.AVAILABLE);
        
        // High inbound flow to ensure holding builds quickly
        ScenarioConfig config = new ScenarioConfig(60,60.0,0.0,30,seed,List.of(takeoffOnly));
        SimulationResult result = new SimulationService().run(config);

        assertTrue(result.getMaxHoldingPatternSize() > 0, "Expected holding pattern to build up");
        assertTrue(result.getDivertedCount() > 0, "Expected diversions when fuel drops below threshold (FR16)");
    }

    
    @Test
    void TC06_FR11_twoRunwaysVsOneRunway_peakQueuesShouldNotIncrease() {
        long seed = 123L;

        ScenarioConfig oneRunway = new ScenarioConfig(180, 30.0, 30.0, 30, seed, List.of(new Runway("09", 3500, 90, RunwayOperatingMode.MIXED, RunwayOperationalStatus.AVAILABLE)));
        ScenarioConfig twoRunways = new ScenarioConfig(180, 30.0, 30.0, 30, seed, List.of(new Runway("09", 3500, 90, RunwayOperatingMode.MIXED, RunwayOperationalStatus.AVAILABLE),new Runway("27", 3500, 270, RunwayOperatingMode.MIXED, RunwayOperationalStatus.AVAILABLE)));

        SimulationResult a = new SimulationService().run(oneRunway);
        SimulationResult b = new SimulationService().run(twoRunways);

        assertTrue(b.getMaxTakeoffQueueSize() <= a.getMaxTakeoffQueueSize(),"Expected takeoff queue peak not to increase with more runways");
        assertTrue(b.getMaxHoldingPatternSize() <= a.getMaxHoldingPatternSize(),"Expected holding pattern peak not to increase with more runways");
    }

    @Test
    void TC06_FR13_FR15_FR16_runwayClosedByStatus_causesCancellationsAndDiversions() {
        long seed = 7L;
        // Even though mode is MIXED, status blocks all usage via isAvailableAt()
        Runway closed = new Runway("09", 3500, 90, RunwayOperatingMode.MIXED, RunwayOperationalStatus.RUNWAY_INSPECTION);

        ScenarioConfig config = new ScenarioConfig(120,60.0,60.0,5,seed,List.of(closed));
        SimulationResult result = new SimulationService().run(config);

        assertTrue(result.getMaxTakeoffQueueSize() > 0 || result.getMaxHoldingPatternSize() > 0,"Expected queues to build when runway is closed (FR13)");
        assertTrue(result.getCancelledCount() > 0,"Expected cancellations when takeoffs never happen and threshold is small (FR15)");
        assertTrue(result.getDivertedCount() > 0,"Expected diversions when landings never happen and fuel drops below threshold (FR16)");
    }

    @Test
    void TC06_FR15_thresholdTooHigh_noCancellationsEvenIfTakeoffImpossible() {
        long seed = 11L;

        Runway landingOnly = new Runway("09", 3500, 90, RunwayOperatingMode.LANDING, RunwayOperationalStatus.AVAILABLE);
        ScenarioConfig config = new ScenarioConfig(60, 0.0, 60.0, 10000, seed, List.of(landingOnly));
        SimulationResult result = new SimulationService().run(config);

        assertEquals(0, result.getCancelledCount(),"Expected zero cancellations when cancellation threshold exceeds simulation duration");
        assertTrue(result.getMaxTakeoffQueueSize() > 0,"Expected takeoff queue to build if takeoffs are impossible");
    }

    @Test
    void TC06_FR16_shortDuration_noDiversionsEvenIfLandingImpossible() {
        long seed = 22L;
        Runway takeoffOnly = new Runway("09", 3500, 90, RunwayOperatingMode.TAKEOFF, RunwayOperationalStatus.AVAILABLE);
        ScenarioConfig config = new ScenarioConfig(5, 60.0, 0.0, 30, seed, List.of(takeoffOnly));
        SimulationResult result = new SimulationService().run(config);

        assertTrue(result.getMaxHoldingPatternSize() > 0, "Expected holding to exist when landings are impossible");
        assertEquals(0, result.getDivertedCount(),"Expected zero diversions in a very short sim (fuel cannot drop to diversion threshold)");
    }




    // ---------------------------------
    // TC07 — FR04, FR09, FR17 (Metrics)
    // ---------------------------------

    /**
     * With zero inbound and outbound traffic, the simulation should produce
     * zero metrics across queues, delays, and disruptions.
     *
     * TC07: baseline sanity test (no traffic scenario).
     */
    @Test
    void TC07_zeroTraffic_returnsAllZeroMetrics(){
        long seed = 42L;
        Runway runway = new Runway("09", 3500, 90,RunwayOperatingMode.MIXED, RunwayOperationalStatus.AVAILABLE);
        ScenarioConfig config = new ScenarioConfig(60,0.0,0.0,30,seed,List.of(runway));

        SimulationResult result = new SimulationService().run(config);

        // Departure metrics (FR04)
        assertEquals(0, result.getMaxTakeoffQueueSize());
        assertEquals(0.0, result.getAvgTakeoffQueueWaitMinutes(), 1e-9);
        assertEquals(0L, result.getMaxDepartureDelayMinutes());
        assertEquals(0.0, result.getAvgDepartureDelayMinutes(), 1e-9);

        // Arrival metrics (FR09)
        assertEquals(0, result.getMaxHoldingPatternSize());
        assertEquals(0.0, result.getAvgHoldingTimeMinutes(), 1e-9);
        assertEquals(0L, result.getMaxArrivalDelayMinutes());
        assertEquals(0.0, result.getAvgArrivalDelayMinutes(), 1e-9);

        // Disruptions (FR17)
        assertEquals(0, result.getCancelledCount());
        assertEquals(0, result.getDivertedCount());
    }

    /**
     * Sanity test: with non-zero traffic, the simulation should show some activity
     * (queues build up at some point) and all reported metrics should remain non-negative.
     *
     * TC07: general metrics consistency check.
     */
    @Test
    void run_nonZeroTraffic_metricsAreNonNegativeAndActivityOccurs_TC07() {
        long seed = 99L;
        Runway runway = new Runway("09", 3500, 90,RunwayOperatingMode.MIXED,RunwayOperationalStatus.AVAILABLE);
        
        // Non-zero traffic rates to generate aircraft
        ScenarioConfig config = new ScenarioConfig(180,2.0,2.0,60,seed,List.of(runway));
        SimulationResult result = new SimulationService().run(config);

        // “Activity occurred” (at least one queue built at some point)
        assertTrue(result.getMaxTakeoffQueueSize() > 0 || result.getMaxHoldingPatternSize() > 0,"Expected some activity (aircraft generated should enter queues at least briefly)");

        // Metrics should never be negative
        assertTrue(result.getAvgTakeoffQueueWaitMinutes() >= 0.0);
        assertTrue(result.getAvgDepartureDelayMinutes() >= 0.0);
        assertTrue(result.getAvgHoldingTimeMinutes() >= 0.0);
        assertTrue(result.getAvgArrivalDelayMinutes() >= 0.0);

        assertTrue(result.getMaxDepartureDelayMinutes() >= 0);
        assertTrue(result.getMaxArrivalDelayMinutes() >= 0);

        assertTrue(result.getCancelledCount() >= 0);
        assertTrue(result.getDivertedCount() >= 0);
    }
    

    // ---------------------------------
    // TC08 — FR02, FR06 (Repeatability)
    // ---------------------------------


    /**
     * Validates repeatability: same seed + same config should produce identical results.
     * This supports the project's repeatability requirement (important for validation).
     */
    @Test
    void TC08_sameSeedSameConfig_sameResults(){
        long seed = 12345L;

        Runway runway1 = new Runway("09",3500,90,RunwayOperatingMode.MIXED,RunwayOperationalStatus.AVAILABLE);
        Runway runway2 = new Runway("09",3500,90,RunwayOperatingMode.MIXED,RunwayOperationalStatus.AVAILABLE);

        ScenarioConfig c1 = new ScenarioConfig(180,30.0,30.0,30,seed,List.of(runway1));
        ScenarioConfig c2 = new ScenarioConfig(180,30.0,30.0,30,seed,List.of(runway2));

        SimulationResult r1 = new SimulationService().run(c1);
        SimulationResult r2 = new SimulationService().run(c2);

        assertResultsEqual(r1, r2);
    }

    /**
     * Helper assertion to compare two SimulationResult objects field-by-field.
     * Floating-point fields use a small tolerance to avoid precision issues.
     */
    private void assertResultsEqual(SimulationResult a, SimulationResult b) {
        assertEquals(a.getMaxTakeoffQueueSize(), b.getMaxTakeoffQueueSize());
        assertEquals(a.getAvgTakeoffQueueWaitMinutes(), b.getAvgTakeoffQueueWaitMinutes(), 1e-9);
        assertEquals(a.getMaxDepartureDelayMinutes(), b.getMaxDepartureDelayMinutes());
        assertEquals(a.getAvgDepartureDelayMinutes(), b.getAvgDepartureDelayMinutes(), 1e-9);

        assertEquals(a.getMaxHoldingPatternSize(), b.getMaxHoldingPatternSize());
        assertEquals(a.getAvgHoldingTimeMinutes(), b.getAvgHoldingTimeMinutes(), 1e-9);
        assertEquals(a.getMaxArrivalDelayMinutes(), b.getMaxArrivalDelayMinutes());
        assertEquals(a.getAvgArrivalDelayMinutes(), b.getAvgArrivalDelayMinutes(), 1e-9);

        assertEquals(a.getCancelledCount(), b.getCancelledCount());
        assertEquals(a.getDivertedCount(), b.getDivertedCount());
    }

    // -------------------------------------------------------
    // TC09 — NFR04, NFR05 (Input validation / error handling)
    // -------------------------------------------------------

    @Test
    void TC09_invalidDuration_throws() {
        Runway r = new Runway("09", 3500, 90, RunwayOperatingMode.MIXED, RunwayOperationalStatus.AVAILABLE);

        assertThrows(IllegalArgumentException.class, () ->
                new ScenarioConfig(0, 10.0, 10.0, 30, 1L, List.of(r))
        );
    }

    @Test
    void TC09_negativeInboundFlow_throws() {
        Runway r = new Runway("09", 3500, 90, RunwayOperatingMode.MIXED, RunwayOperationalStatus.AVAILABLE);

        assertThrows(IllegalArgumentException.class, () ->
                new ScenarioConfig(60, -1.0, 10.0, 30, 1L, List.of(r))
        );
    }

    @Test
    void TC09_negativeOutboundFlow_throws() {
        Runway r = new Runway("09", 3500, 90, RunwayOperatingMode.MIXED, RunwayOperationalStatus.AVAILABLE);

        assertThrows(IllegalArgumentException.class, () ->
                new ScenarioConfig(60, 10.0, -1.0, 30, 1L, List.of(r))
        );
    }

    @Test
    void TC09_invalidCancellationThreshold_throws() {
        Runway r = new Runway("09", 3500, 90, RunwayOperatingMode.MIXED, RunwayOperationalStatus.AVAILABLE);

        assertThrows(IllegalArgumentException.class, () ->
                new ScenarioConfig(60, 10.0, 10.0, 0, 1L, List.of(r))
        );
    }

    @Test
    void TC09_noRunways_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new ScenarioConfig(60, 10.0, 10.0, 30, 1L, List.of())
        );
    }

    @Test
    void TC09_tooManyRunways_throws() {
        List<Runway> runways = java.util.stream.IntStream.rangeClosed(1, 11)
                .mapToObj(i -> new Runway("0" + i, 3500, 90, RunwayOperatingMode.MIXED, RunwayOperationalStatus.AVAILABLE))
                .toList();

        assertThrows(IllegalArgumentException.class, () ->
                new ScenarioConfig(60, 10.0, 10.0, 30, 1L, runways)
        );
    }

}