package com.app.controller;

import com.app.dto.FuelHistoriesDto;
import com.app.dto.FuelHistoryDto;
import com.app.security.SecurityUtil;
import com.app.service.FuelHistoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/fuel/history")
public class FuelHistoryController {

    private final FuelHistoryService fuelHistoryService;
    private final SecurityUtil securityUtil;

    public FuelHistoryController(FuelHistoryService fuelHistoryService, SecurityUtil securityUtil) {
        this.fuelHistoryService = fuelHistoryService;
        this.securityUtil = securityUtil;
    }

    @GetMapping
    public ResponseEntity<?> getAllVehicleFuelHistories(@RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam Long vehicleId) {
        if (securityUtil.getSessionUser() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        Page<FuelHistoryDto> fuelHistoryDtos = fuelHistoryService.getAllHistories(vehicleId, page);
        if (fuelHistoryDtos.isEmpty())
            return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "No fuel history found"));
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("body", new FuelHistoriesDto(fuelHistoryDtos)));
    }

    @PostMapping
    public ResponseEntity<?> addFuelHistory(@RequestBody @Valid FuelHistoryDto fuelHistoryDto) {
        if (securityUtil.getSessionUser() == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        FuelHistoryDto created = fuelHistoryService.addFuelHistory(fuelHistoryDto);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Successfully added fuel history",
                "body", created));
    }
}
