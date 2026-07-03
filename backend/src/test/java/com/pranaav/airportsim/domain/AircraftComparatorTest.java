package com.pranaav.airportsim.domain;

import org.junit.jupiter.api.Test;
import com.pranaav.airportsim.enums.EmergencyStatus;

import static org.junit.jupiter.api.Assertions.*;

class AircraftComparatorTest{
    // TC04 / FR08: emergencies have higher priority than normal aircraft
    @Test
    void compare_emergency_Higher_than_Normal() {
        Aircraft normal = Aircraft.createInbound("N1", "OP", "AAA", "BBB", 10, EmergencyStatus.NONE, 30);
        Aircraft emergency = Aircraft.createInbound("E1", "OP", "AAA", "BBB", 10, EmergencyStatus.FUEL, 30);
        AircraftComparator cmp = new AircraftComparator();
        assertTrue(cmp.compare(emergency, normal) < 0);
        assertTrue(cmp.compare(normal, emergency) > 0);
    }

    // TC04 / FR08: emergency severity uses enum order (FUEL before MECHANICAL before PASSENGER_HEALTH)
    @Test
    void compare_emergencySeverityByEnumOrder() {
        Aircraft fuel = Aircraft.createInbound("E1", "OP", "AAA", "BBB", 10, EmergencyStatus.FUEL, 30);
        Aircraft mech = Aircraft.createInbound("E2", "OP", "AAA", "BBB", 10, EmergencyStatus.MECHANICAL_FAILURE, 30);
        Aircraft health = Aircraft.createInbound("E3", "OP", "AAA", "BBB", 10, EmergencyStatus.PASSENGER_HEALTH, 30);
        AircraftComparator cmp = new AircraftComparator();
        assertTrue(cmp.compare(fuel, mech) < 0);
        assertTrue(cmp.compare(mech, health) < 0);
    }

    // TC04 / FR08: tie-breaker is scheduled time
    @Test
    void compare_sameEmergencyType_tieBreakByScheduledTime() {
        Aircraft a10 = Aircraft.createInbound("A10", "OP", "AAA", "BBB", 10, EmergencyStatus.NONE, 30);
        Aircraft a20 = Aircraft.createInbound("A20", "OP", "AAA", "BBB", 20, EmergencyStatus.NONE, 30);
        AircraftComparator cmp = new AircraftComparator();
        assertTrue(cmp.compare(a10, a20) < 0);
        assertTrue(cmp.compare(a20, a10) > 0);
    }

}