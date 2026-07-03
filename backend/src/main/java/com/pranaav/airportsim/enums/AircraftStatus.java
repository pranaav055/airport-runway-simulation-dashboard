package com.pranaav.airportsim.enums;

/**
 * Represents the lifecycle status of an aircraft within the simulation.
 *
 * Aircraft transition through these states as they move through the
 * simulation process from scheduling to completion or disruption.
 */
public enum AircraftStatus {
    SCHEDULED,   // Aircraft has been generated but has not yet entered a queue 
    QUEUED,      // Aircraft is waiting in either the holding pattern (inbound) or take-off queue (outbound) 
    SERVED,      // Aircraft has successfully used a runway and completed its operation (landing or takeoff) 
    CANCELLED,   // Outbound aircraft cancelled due to excessive waiting time in the take-off queue 
    DIVERTED     // Inbound aircraft diverted because fuel dropped below the minimum safe threshold 
}