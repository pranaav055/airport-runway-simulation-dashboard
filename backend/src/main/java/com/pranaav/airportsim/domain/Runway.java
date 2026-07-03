package com.pranaav.airportsim.domain;

import com.pranaav.airportsim.enums.RunwayOperatingMode;
import com.pranaav.airportsim.enums.RunwayOperationalStatus;

/**
 * Represents a runway within the airport simulation.
 *
 * A runway has fixed physical characteristics such as runway number,
 * length, and bearing. It also has dynamic operational properties
 * that may change during the simulation, such as operating mode
 * and operational status.
 *
 * The runway tracks whether it is currently occupied by storing the
 * simulation time until which it remains in use.
 */
public class Runway {

    private final String runwayNumber;      // Runway identifier (e.g. "09", "27", "04L")
    private final int lengthMetres;         // Physical length of the runway in metres
    private final int bearingDegrees;       // Runway orientation in degrees relative to north

    private RunwayOperatingMode operatingMode;          // Current runway use * (e.g. landing-only, takeoff-only, or mixed operations).
    private RunwayOperationalStatus operationalStatus;  // Indicates whether the runway is operational. (unavailable due to disruptions etc.)

    /**
     * Stores the simulation time (in minutes) until which the runway
     * remains occupied by an aircraft.
     *
     * If the current simulation time is greater than or equal to this
     * value, the runway is considered free.
     */
    private long occupiedUntilMinutes;

    /**
     * Constructs a new Runway instance with specified physical
     * and operational properties.
     *
     * @param runwayNumber runway identifier (e.g. "09")
     * @param lengthMetres physical runway length in metres
     * @param bearingDegrees runway orientation in degrees
     * @param operatingMode current runway operating mode
     * @param operationalStatus current runway availability status
     */
    public Runway(
            String runwayNumber,
            int lengthMetres,
            int bearingDegrees,
            RunwayOperatingMode operatingMode,
            RunwayOperationalStatus operationalStatus
    ) {
        // Basic validation to ensure runway data is valid
        if (runwayNumber == null || runwayNumber.isBlank())
            throw new IllegalArgumentException("Runway number cannot be blank");
        if (lengthMetres <= 0)
            throw new IllegalArgumentException("Runway length must be > 0");
        if (bearingDegrees < 0 || bearingDegrees >= 360)
            throw new IllegalArgumentException("Runway bearing must be in [0, 359]");
        if (operatingMode == null)
            throw new IllegalArgumentException("Runway operating mode cannot be null");
        if (operationalStatus == null)
            throw new IllegalArgumentException("Runway operational status cannot be null");

        this.runwayNumber = runwayNumber;
        this.lengthMetres = lengthMetres;
        this.bearingDegrees = bearingDegrees;
        this.operatingMode = operatingMode;
        this.operationalStatus = operationalStatus;

        // Initially the runway is free at the start of the simulation
        this.occupiedUntilMinutes = 0L;
    }

    // Returns the runway identifier
    public String getRunwayNumber() { return runwayNumber; }

    // Returns the runway length in metres
    public int getLengthMetres() { return lengthMetres; }

    // Returns the runway bearing in degrees
    public int getBearingDegrees() { return bearingDegrees; }

    // Returns the current operating mode of the runway
    public RunwayOperatingMode getOperatingMode() { return operatingMode; }

    /**
     * Updates the runway operating mode.
     *
     * This may occur during the simulation if operational
     * procedures change (e.g. switching to landing-only mode).
     */
    public void setOperatingMode(RunwayOperatingMode operatingMode) {
        if (operatingMode == null) throw new IllegalArgumentException("Runway operating mode cannot be null");
        this.operatingMode = operatingMode;
    }

    // Returns the current operational status of the runway
    public RunwayOperationalStatus getOperationalStatus() { return operationalStatus; }

    /**
     * Updates the operational status of the runway.
     *
     * This is used to simulate events such as runway closures,
     * maintenance periods, or other disruptions.
     */
    public void setOperationalStatus(RunwayOperationalStatus operationalStatus) {
        if (operationalStatus == null) throw new IllegalArgumentException("Runway operational status cannot be null");
        this.operationalStatus = operationalStatus;
    }

    // Returns the time until which the runway remains occupied
    public long getOccupiedUntilMinutes() { return occupiedUntilMinutes; }

    /**
     * Determines whether the runway is available at a given
     * simulation time.
     *
     * A runway is available only if:
     *  - It is operational, and
     *  - The requested time is after the current occupation period.
     *
     * @param timeMinutes current simulation time
     * @return true if the runway can be used
     */
    public boolean isAvailableAt(long timeMinutes) {
        return operationalStatus == RunwayOperationalStatus.AVAILABLE && timeMinutes >= occupiedUntilMinutes;
    }

    /**
     * Marks the runway as occupied until a specified time.
     *
     * This is called when an aircraft begins using the runway
     * for landing or takeoff.
     *
     * @param timeMinutes simulation time when the runway will become free
     */
    public void occupyUntil(long timeMinutes) {
        if (timeMinutes < 0) throw new IllegalArgumentException("timeMinutes cannot be negative");
        this.occupiedUntilMinutes = timeMinutes;
    }

    /**
     * Checks whether the runway is vacant at a given simulation time.
     *
     * Unlike {@link #isAvailableAt(long)}, this method ignores
     * the runway operational status and only checks occupancy.
     *
     * @param timeMinutes simulation time to check
     * @return true if the runway is not currently occupied
     */
    public boolean isVacant(long timeMinutes) {
        if (timeMinutes < 0) throw new IllegalArgumentException("timeMinutes cannot be negative");
        if (timeMinutes >= occupiedUntilMinutes) {
            return true;
        }
        return false;
    }
}
