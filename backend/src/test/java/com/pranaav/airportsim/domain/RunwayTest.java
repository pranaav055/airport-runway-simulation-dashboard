package com.pranaav.airportsim.domain;

import org.junit.jupiter.api.Test;

import com.pranaav.airportsim.enums.RunwayOperatingMode;
import com.pranaav.airportsim.enums.RunwayOperationalStatus;

import static org.junit.jupiter.api.Assertions.*;

class RunwayTest{ 
    // TC02 / FR12: runway exclusivity + availability logic
    @Test
    void isAvailableAt_availableAndNotOccupied_true() {
        Runway r = new Runway("09", 3500, 90,RunwayOperatingMode.MIXED,RunwayOperationalStatus.AVAILABLE);
        assertTrue(r.isAvailableAt(0));
        assertTrue(r.isAvailableAt(10));
    }

    // TC02 / FR12: once occupied, runway not available until occupiedUntil
    @Test
    void isAvailableAt_occupied_falseUntilTimeReached() {
        Runway r = new Runway("09", 3500, 90,RunwayOperatingMode.MIXED,RunwayOperationalStatus.AVAILABLE);
        r.occupyUntil(15);
        assertFalse(r.isAvailableAt(0));
        assertFalse(r.isAvailableAt(14));
        assertTrue(r.isAvailableAt(15));
        assertTrue(r.isAvailableAt(16));
    }

    // FR11/FR13-ish: if runway not AVAILABLE, cannot be used even if vacant
    @Test
    void isAvailableAt_notOperational_false() {
        Runway r = new Runway("09", 3500, 90,RunwayOperatingMode.MIXED,RunwayOperationalStatus.SNOW_CLEARANCE);
        assertFalse(r.isAvailableAt(0));
        assertFalse(r.isAvailableAt(100));
    }

    @Test
    void occupyUntil_negative_throws() {
        Runway r = new Runway("09", 3500, 90,RunwayOperatingMode.MIXED,RunwayOperationalStatus.AVAILABLE);
        assertThrows(IllegalArgumentException.class, () -> r.occupyUntil(-1));
    }

    @Test
    void isVacant_negativeTime_throws() {
        Runway r = new Runway("09", 3500, 90,RunwayOperatingMode.MIXED,RunwayOperationalStatus.AVAILABLE);
        assertThrows(IllegalArgumentException.class, () -> r.isVacant(-1));
    }

    @Test
    void setOperatingMode_null_throws() {
        Runway r = new Runway("09", 3500, 90,RunwayOperatingMode.MIXED,RunwayOperationalStatus.AVAILABLE);
        assertThrows(IllegalArgumentException.class, () -> r.setOperatingMode(null));
    }

    @Test
    void setOperationalStatus_null_throws() {
        Runway r = new Runway("09", 3500, 90,RunwayOperatingMode.MIXED,RunwayOperationalStatus.AVAILABLE);
        assertThrows(IllegalArgumentException.class, () -> r.setOperationalStatus(null));
    }

}