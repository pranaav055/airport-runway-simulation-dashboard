package com.pranaav.airportsim.domain;

import com.pranaav.airportsim.enums.AircraftType;

/**
 * Represents a runway usage event during the simulation.
 *
 * Each event records when a specific aircraft occupies a runway,
 * including the time interval of the operation, the runway used,
 * the aircraft callsign, and whether the operation was a landing
 * or takeoff.
 *
 * These events can be used to reconstruct the timeline of runway
 * activity for analysis or visualisation of simulation results.
 */
public class RunwayUseEvent {

    private final long startMinute;    // Time the runway operation begins (minutes since simulation start)
    private final long endMinute;      // Time the runway operation finishes
    private final String runwayNumber; // Identifier of the runway being used (e.g. "09", "27")
    private final String callsign;     // Callsign of the aircraft using the runway
    private final AircraftType aircraftType;    // Type of aircraft operation (INBOUND = landing, OUTBOUND = takeoff)

     /**
     * Creates a new runway usage event.
     *
     * @param startMinute simulation minute when runway usage begins
     * @param endMinute simulation minute when runway becomes free
     * @param runwayNumber runway identifier
     * @param callsign aircraft identifier
     * @param aircraftType inbound (landing) or outbound (takeoff)
     */
    public RunwayUseEvent(long startMinute, long endMinute, String runwayNumber, String callsign, AircraftType aircraftType) {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.runwayNumber = runwayNumber;
        this.callsign = callsign;
        this.aircraftType = aircraftType;
    }

    public long getStartMinute() { return startMinute; }
    public long getEndMinute() { return endMinute; }
    public String getRunwayNumber() { return runwayNumber; }
    public String getCallsign() { return callsign; }
    public AircraftType getAircraftType() { return aircraftType; }
}