package com.comp5348.bank.controller;

import com.comp5348.bank.service.FaultInjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for managing fault injection during testing.
 */
@Slf4j
@RestController
@RequestMapping("/api/faults")
@RequiredArgsConstructor
public class FaultController {

    private final FaultInjectionService faultInjectionService;

    @PostMapping("/enable")
    public ResponseEntity<String> enableFaults(@RequestParam(defaultValue = "0.3") double probability) {
        faultInjectionService.enableFaults(probability);
        return ResponseEntity.ok("Fault injection enabled with probability: " + probability);
    }

    @PostMapping("/disable")
    public ResponseEntity<String> disableFaults() {
        faultInjectionService.disableFaults();
        return ResponseEntity.ok("Fault injection disabled");
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getFaultStatus() {
        return ResponseEntity.ok(Map.of("enabled", faultInjectionService.isFaultEnabled()));
    }
}
