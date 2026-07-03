package com.pranaav.airportsim.domain;

import java.util.List;

/**
 * DTO matching the frontend payload for simulation configuration.
 */
public class ScenarioConfigRequest {
    private long durationMinutes;
    private double inboundFlowPerHour;
    private double outboundFlowPerHour;
    private long cancellationThresholdMinutes;
    private long randomSeed;
    private boolean emergencyPriority;
    private List<RunwayConfig> runways;

    public long getDurationMinutes() { return durationMinutes; }
    public double getInboundFlowPerHour() { return inboundFlowPerHour; }
    public double getOutboundFlowPerHour() { return outboundFlowPerHour; }
    public long getCancellationThresholdMinutes() { return cancellationThresholdMinutes; }
    public long getRandomSeed() { return randomSeed; }
    public boolean isEmergencyPriority() { return emergencyPriority; }
    public List<RunwayConfig> getRunways() { return runways; }

    public static class RunwayConfig {
        private int id;
        private String mode;
        private String status;

        public int getId() { return id; }
        public String getMode() { return mode; }
        public String getStatus() { return status; }
    }
}
