package com.pranaav.airportsim.enums;

/**
 * Represents the emergency condition of an aircraft.
 *
 * Emergency aircraft are prioritised in the holding pattern when allocating
 * runways. The enum order defines the relative priority when two aircraft
 * both have emergencies (used by AircraftComparator).
 */
public enum EmergencyStatus {
    NONE,               // No emergency condition; aircraft follows normal queue rules 
    FUEL,               // Aircraft is low on fuel and requires priority landing 
    MECHANICAL_FAILURE, // Aircraft has experienced a mechanical failure requiring urgent landing 
    PASSENGER_HEALTH    // Medical emergency involving a passenger requiring priority landing 
} 

