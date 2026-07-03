package com.pranaav.airportsim.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.pranaav.airportsim.enums.RunwayOperatingMode;
import com.pranaav.airportsim.enums.RunwayOperationalStatus;
import com.pranaav.airportsim.interfaces.SimulationServiceInterface;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * REST controller exposing simulation functionality to the frontend.
 *
 * Base path: /api/simulation
 */
@RestController
@RequestMapping("/api/simulation")
@CrossOrigin(origins = "*")
public class SimulationController {

    private final SimulationServiceInterface simulationService;

    @Autowired
    public SimulationController(SimulationServiceInterface simulationService) {
        this.simulationService = simulationService;
    }

    /**
     * Runs a simulation using the scenario configuration provided by the frontend.
     *
     * Endpoint: POST /api/simulation/run
     */
    @PostMapping("/run")
    public ResponseEntity<?> handleSimulationRun(@RequestBody ScenarioConfigRequest request) {
        if (request.getDurationMinutes() <= 0) {
            return ResponseEntity.badRequest().body("Duration must be greater than 0.");
        }

        ScenarioConfig config = new ScenarioConfig(
                request.getDurationMinutes(),
                request.getInboundFlowPerHour(),
                request.getOutboundFlowPerHour(),
                request.getCancellationThresholdMinutes(),
                request.getRandomSeed(),
                mapRunways(request.getRunways())
        );

        SimulationResult result = simulationService.calculateSimulation(config);
        return ResponseEntity.ok(result);
    }

    private List<Runway> mapRunways(List<ScenarioConfigRequest.RunwayConfig> runways) {
        if (runways == null) return List.of();
        return runways.stream().map((r) -> {
            String runwayNumber = String.format("%02d", r.getId());
            int lengthMetres = 3000;
            int bearingDegrees = (90 + (r.getId() - 1) * 10) % 360;
            RunwayOperatingMode operatingMode = parseMode(r.getMode());
            RunwayOperationalStatus operationalStatus = parseStatus(r.getStatus());
            return new Runway(runwayNumber, lengthMetres, bearingDegrees, operatingMode, operationalStatus);
        }).collect(Collectors.toList());
    }

    private RunwayOperatingMode parseMode(String mode) {
        if (mode == null) return RunwayOperatingMode.MIXED;
        String normalized = mode.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("LANDING")) return RunwayOperatingMode.LANDING;
        if (normalized.equals("TAKE-OFF") || normalized.equals("TAKEOFF")) return RunwayOperatingMode.TAKEOFF;
        if (normalized.equals("MIXED")) return RunwayOperatingMode.MIXED;
        return RunwayOperatingMode.MIXED;
    }

    private RunwayOperationalStatus parseStatus(String status) {
        if (status == null) return RunwayOperationalStatus.AVAILABLE;
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("ENABLED")) return RunwayOperationalStatus.AVAILABLE;
        if (normalized.equals("CLOSED")) return RunwayOperationalStatus.UNAVAILABLE;
        return RunwayOperationalStatus.AVAILABLE;
    }
}
