package com.pranaav.airportsim.domain;
import com.pranaav.airportsim.enums.AircraftStatus;
import com.pranaav.airportsim.enums.AircraftType;
import com.pranaav.airportsim.enums.EmergencyStatus;

/**
 * Represents an aircraft participating in the airport simulation.
 *
 * This class stores both the static properties of an aircraft (such as
 * callsign, origin, destination and type) and dynamic simulation data
 * such as queue entry time, actual landing/takeoff time, and remaining fuel.
 *
 * Aircraft objects cannot be instantiated directly. Instead, they must be
 * created through the factory methods:
 *  - createInbound(...)
 *  - createOutbound(...)
 *
 * This ensures that aircraft are always created with valid configuration
 * depending on whether they are inbound or outbound.
 */
public class Aircraft {

    private final String callsign;      // Unique identifier used for communication with air traffic control
    private final String operator;      // Airline or operator of the aircraft
    private final String origin;        // Airport where the aircraft originated
    private final String destination;   // Airport where the aircraft is heading

    // Scheduled time for the aircraft event (arrival or departure) measured in minutes from simulation start
    private final long scheduledTimeMinutes;

    // optional runtime fields
    private Long queueEntryTimeMinutes; // The time (in simulation minutes) when the aircraft entered a runway queue.
    private Long actualTimeMinutes;     // The actual time when the aircraft completed its operation

    // Remaining fuel represented as minutes of flight time for inbound only. (easier than litres for rules)
    private double fuelMinutesRemaining;

    // keep simple for now
    private final EmergencyStatus emergencyType;        // Indicates whether the aircraft is experiencing an emergency
    private final AircraftType aircraftType;            // Indicates whether the aircraft is inbound (landing) or outbound (departure)
    private AircraftStatus status;                      // Current state of the aircraft within the simulation

    /**
     * Private constructor used internally by the factory methods.
     * This prevents direct creation of generic aircraft objects
     * and ensures all aircraft are either inbound or outbound.
     *
     * Performs validation on all input parameters to ensure the
     * simulation always receives valid aircraft data.
     */
    private Aircraft(
            String callsign,
            String operator,
            String origin,
            String destination,
            AircraftType aircraftType,
            long scheduledTimeMinutes,
            EmergencyStatus emergencyType,
            double fuelMinutesRemaining
    ) {
        // Basic validation to prevent invalid aircraft creation (keep it lightweight)
        if (callsign == null || callsign.isBlank()) throw new IllegalArgumentException("Callsign cannot be blank");
        if (operator == null || operator.isBlank()) throw new IllegalArgumentException("Operator cannot be blank");
        if (origin == null || origin.isBlank()) throw new IllegalArgumentException("Origin cannot be blank");
        if (destination == null || destination.isBlank()) throw new IllegalArgumentException("Destination cannot be blank");
        if (aircraftType == null) throw new IllegalArgumentException("Aircraft type cannot be null");
        if (scheduledTimeMinutes < 0) throw new IllegalArgumentException("Scheduled time cannot be negative");
        if (fuelMinutesRemaining < 0) throw new IllegalArgumentException("Fuel cannot be negative");
        if (emergencyType == null) throw new IllegalArgumentException("Emergency status cannot be null");

        // Assign immutable aircraft information
        this.callsign = callsign;
        this.operator = operator;
        this.origin = origin;
        this.destination = destination;

        this.aircraftType = aircraftType;
        this.scheduledTimeMinutes = scheduledTimeMinutes;
        this.emergencyType = emergencyType;

        this.fuelMinutesRemaining = fuelMinutesRemaining;

        // Default simulation state when aircraft is first created
        this.status = AircraftStatus.SCHEDULED;
        this.queueEntryTimeMinutes = null;
        this.actualTimeMinutes = null;
    }

    /**
     * Factory method for creating inbound aircraft.
     *
     * Inbound aircraft must have fuel levels within a valid operational range
     * so that the simulation can model holding patterns, emergency landings,
     * or diversions.
     *
     * @param callsign aircraft identifier
     * @param operator airline operator
     * @param origin airport where the aircraft departed
     * @param destination arrival airport
     * @param scheduledTimeMinutes scheduled arrival time
     * @param emergencyType emergency condition (if any)
     * @param fuelMinutesRemaining remaining fuel expressed as minutes
     * @return a valid inbound Aircraft object
     */
    public static Aircraft createInbound(
            String callsign,
            String operator,
            String origin,
            String destination,
            long scheduledTimeMinutes,
            EmergencyStatus emergencyType,
            double fuelMinutesRemaining
    ) {
        // Enforce inbound aircraft fuel constraints required by simulation rules
        if (fuelMinutesRemaining < 20 || fuelMinutesRemaining > 60) throw new IllegalArgumentException("Inbound fuel must be 20-60 minutes worth");
        return new Aircraft(callsign, operator, origin, destination,
                AircraftType.INBOUND, scheduledTimeMinutes, emergencyType, fuelMinutesRemaining);
    }

     /**
     * Factory method for creating outbound aircraft.
     *
     * Outbound aircraft do not track fuel for the purposes of this simulation,
     * so fuel is set to zero.
     */
    public static Aircraft createOutbound(
            String callsign,
            String operator,
            String origin,
            String destination,
            long scheduledTimeMinutes,
            EmergencyStatus emergencyType
    ) {
        return new Aircraft(callsign, operator, origin, destination,
                AircraftType.OUTBOUND, scheduledTimeMinutes, emergencyType, 0.0);
    }
   
   // Returns the aircraft callsign
   public String getCallsign() { return callsign; }

   // Returns whether the aircraft is inbound or outbound
   public AircraftType getAircraftType() { return aircraftType; }

   // Returns the scheduled arrival or departure time
   public long getScheduledTimeMinutes() { return scheduledTimeMinutes; }

   // Returns true if the aircraft is currently in an emergency state
   public boolean isEmergency() { return emergencyType != EmergencyStatus.NONE; }

   // Returns the aircraft's current simulation status
   public AircraftStatus getStatus() { return status; }

   // Updates the simulation status of the aircraft
   public void setStatus(AircraftStatus status) { this.status = status; }

   // Returns the time the aircraft entered a runway queue
   public Long getQueueEntryTimeMinutes() { return queueEntryTimeMinutes; }

   // Records when the aircraft entered a queue
   public void setQueueEntryTimeMinutes(Long t) { this.queueEntryTimeMinutes = t; }

   // Returns the time the aircraft completed its operation
   public Long getActualTimeMinutes() { return actualTimeMinutes; }

   // Records the time the aircraft completed its landing or takeoff
   public void setActualTimeMinutes(Long t) { this.actualTimeMinutes = t; }

   // Returns the aircraft's emergency condition
   public EmergencyStatus getEmergencyType() { return emergencyType; }
   
   // Returns the remaining fuel expressed as minutes of flight time
   public double getFuelMinutesRemaining() { return fuelMinutesRemaining; }

   /**
     * Updates the aircraft's remaining fuel level.
     * Used during simulation to model fuel consumption while holding.
     */
   public void setFuelMinutesRemaining(double fuelMinutesRemaining) {
      if (fuelMinutesRemaining < 0) throw new IllegalArgumentException("Fuel cannot be negative");
      this.fuelMinutesRemaining = fuelMinutesRemaining;
   }
    

}