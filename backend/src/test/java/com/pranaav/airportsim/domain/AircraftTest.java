package com.pranaav.airportsim.domain;

import org.junit.jupiter.api.Test;
import com.pranaav.airportsim.enums.AircraftStatus;
import com.pranaav.airportsim.enums.AircraftType;
import com.pranaav.airportsim.enums.EmergencyStatus;

import static org.junit.jupiter.api.Assertions.*;

class AircraftTest{
    @Test
    void createInboundAircraft_validateInputs(){
        Aircraft a = Aircraft.createInbound("BA123", "British Airways", "LHR", "EDI", 10L, EmergencyStatus.NONE, 30.0);
        assertEquals("BA123", a.getCallsign());
        assertEquals(AircraftType.INBOUND, a.getAircraftType());
        assertEquals(10L, a.getScheduledTimeMinutes());
        assertFalse(a.isEmergency());
        assertEquals(AircraftStatus.SCHEDULED, a.getStatus());
        assertNull(a.getQueueEntryTimeMinutes());
        assertNull(a.getActualTimeMinutes());
        assertEquals(30.0, a.getFuelMinutesRemaining(), 1e-9);
    }

    @Test
    void createInbound_blank_callsign_throws(){
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createInbound("", "British Airways", "LHR", "EDI", 10L, EmergencyStatus.NONE, 30.0)
        );
    }

    @Test
    void createInbound_blank_operator_throws(){
            assertThrows(IllegalArgumentException.class, () ->
        Aircraft.createInbound("BA123", "", "LHR", "EDI", 10L, EmergencyStatus.NONE, 30.0)
        );
    }

    @Test
    void createInbound_blank_origin_throws(){
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createInbound("BA123", "British Airways", "", "EDI", 10L, EmergencyStatus.NONE, 30.0)
        );
    }

    @Test
    void createInbound_blank_destination_throws(){
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createInbound("BA123", "British Airways", "LHR", "", 10L, EmergencyStatus.NONE, 30.0)
        );
    }

    @Test
    void createInbound_fuelBelow20_throws(){
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createInbound("BA123", "British Airways", "LHR", "EDI", 10L, EmergencyStatus.NONE, 19.999)
        );
    }
    @Test
    void createInbound_fuelAbove60_throws(){
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createInbound("BA123", "British Airways", "LHR", "EDI", 10L, EmergencyStatus.NONE, 60.001)
        );
    }

    @Test
    void createInbound_negativeScheduledTime_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createInbound("BA123", "BA", "LHR", "EDI", -1L, EmergencyStatus.NONE, 30.0)
        );
    }

    @Test
    void createInbound_nullEmergencyStatus_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createInbound("BA123", "BA", "LHR", "EDI", 10L, null, 30.0)
        );
    }

    @Test
    void isEmergency_trueWhenNotNone() {
        Aircraft a = Aircraft.createInbound("BA123", "BA", "LHR", "EDI", 10L, EmergencyStatus.PASSENGER_HEALTH, 30.0);
        assertTrue(a.isEmergency());
    }

    @Test
    void setFuel_negative_throws() {
        Aircraft a = Aircraft.createInbound("BA123", "BA", "LHR", "EDI", 10L, EmergencyStatus.NONE, 30.0);
        assertThrows(IllegalArgumentException.class, () -> a.setFuelMinutesRemaining(-0.01));
    }


    @Test
    void createOutbound_validInputs_setsTypeAndDefaults() {
        Aircraft a = Aircraft.createOutbound("EZY77", "easyJet", "LGW", "AMS", 5L, EmergencyStatus.NONE);
        
        assertEquals("EZY77", a.getCallsign());
        assertEquals(AircraftType.OUTBOUND, a.getAircraftType());
        assertEquals(5L, a.getScheduledTimeMinutes());
        assertFalse(a.isEmergency());

        assertEquals(AircraftStatus.SCHEDULED, a.getStatus());
        assertNull(a.getQueueEntryTimeMinutes());
        assertNull(a.getActualTimeMinutes());
        assertEquals(0.0, a.getFuelMinutesRemaining(), 1e-9);
    }


    @Test
    void createOutbound_blank_callsign_throws(){
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createOutbound("", "easyJet", "LGW", "AMS", 5L, EmergencyStatus.NONE)
        );
    }

    @Test
    void createOutbound_blank_operator_throws(){
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createOutbound("EZY77", "", "LGW", "AMS", 5L, EmergencyStatus.NONE)
        );
    }

    @Test
    void createOutbound_blank_origin_throws(){
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createOutbound("EZY77", "easyJet", "", "AMS", 5L, EmergencyStatus.NONE)
        );
    }

    @Test
    void createOutbound_blank_destination_throws(){
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createOutbound("EZY77", "easyJet", "LGW", "", 5L, EmergencyStatus.NONE)
        );
    }

    @Test
    void createOutbound_nullEmergency_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createOutbound("EZY77", "easyJet", "LGW", "AMS", 5L, null)
        );
    }

    @Test
    void createOutbound_negativeScheduledTime_throws() {
        assertThrows(IllegalArgumentException.class, () ->
            Aircraft.createOutbound("EZY77", "easyJet", "LGW", "AMS", -1L, EmergencyStatus.NONE)
        );
    }

    @Test
    void createOutbound_isEmergency_trueWhenNotNone() {
        Aircraft a = Aircraft.createOutbound("EZY77", "easyJet", "LGW", "AMS", 5L, EmergencyStatus.MECHANICAL_FAILURE);
        assertTrue(a.isEmergency());
    }






}