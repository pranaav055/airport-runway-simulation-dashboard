package com.pranaav.airportsim.domain;

import java.util.Comparator;

/**
 * Comparator to order aircraft for the simulation PriorityQueue.
 * * Logic:
 * 1. Emergencies > Normal flights.
 * 2. Emergency priority follows Enum order (FUEL > MECHANICAL > HEALTH).
 * 3. Scheduled time serves as the final tie-breaker.
 */
public class AircraftComparator implements Comparator<Aircraft> {

    @Override
    public int compare(Aircraft a1, Aircraft a2) {
        if (a1 == null || a2 == null) return 0;

        // Check if either aircraft is in an emergency state
        boolean a1Emergency = a1.isEmergency();
        boolean a2Emergency = a2.isEmergency();

        // Push emergencies to the front of the queue
        if (a1Emergency && !a2Emergency) return -1;
        if (!a1Emergency && a2Emergency) return 1;

        // Both are emergencies: compare based on type severity
        if (a1Emergency && a2Emergency) {
            int emergencyCompare = a1.getEmergencyType().compareTo(a2.getEmergencyType());
            if (emergencyCompare != 0) {
                return emergencyCompare;
            }
        }

        // Default to scheduled time if priorities are equal
        return Long.compare(a1.getScheduledTimeMinutes(), a2.getScheduledTimeMinutes());
    }
}