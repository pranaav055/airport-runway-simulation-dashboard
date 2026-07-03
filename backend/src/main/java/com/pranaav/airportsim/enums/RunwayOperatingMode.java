package com.pranaav.airportsim.enums;

/**
 * Defines how a runway may be used during the simulation.
 *
 * The operating mode determines which type of aircraft
 * the runway is allowed to serve when allocating runway access.
 */
public enum RunwayOperatingMode{
    LANDING,    // Runway is dedicated exclusively to landing aircraft 
    TAKEOFF,    // Runway is dedicated exclusively to aircraft taking off 
    MIXED       // Runway can serve both landing and takeoff operations. 
                // In the simulation logic, landing aircraft are typically prioritised 
                // when both queues contain aircraft.
}