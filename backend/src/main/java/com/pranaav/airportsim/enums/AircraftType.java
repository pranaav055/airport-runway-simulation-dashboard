package com.pranaav.airportsim.enums;

/**
 * Represents whether an aircraft is arriving at or departing from the airport.
 *
 * The aircraft type determines how the aircraft behaves within the simulation.
 * Inbound aircraft enter a holding pattern and attempt to land, while outbound
 * aircraft join a take-off queue waiting for runway clearance.
 */
public enum AircraftType {
    INBOUND,    // Aircraft approaching the airport and waiting to land 
    OUTBOUND    // Aircraft departing from the airport and waiting to take off 

}