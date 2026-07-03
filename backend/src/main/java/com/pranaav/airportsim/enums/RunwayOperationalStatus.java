package com.pranaav.airportsim.enums;

/**
 * Represents the operational condition of a runway.
 *
 * The operational status determines whether the runway can currently
 * be used for aircraft operations within the simulation.
 */
public enum RunwayOperationalStatus {
    AVAILABLE,          // Runway is fully operational and available for aircraft use 
    UNAVAILABLE,        // Runway is unavailable due to unspecified reasons
    RUNWAY_INSPECTION,  // Runway is temporarily unavailable due to routine safety inspection
    SNOW_CLEARANCE      // Runway is unavailable due to equipment malfunction or infrastructure failure
}