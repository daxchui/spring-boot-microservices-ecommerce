
package com.comp5348.bank.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Service for injecting simulated faults to test fault tolerance.
 */
@Slf4j
@Service
public class FaultInjectionService {

    private final Random random = new Random();
    //Change faultEnabled = true; to activate this
    private volatile boolean faultEnabled = false;
    private volatile double faultProbability = 0.1; // 10% failure rate

    /**
     * Randomly injects a fault based on configured probability.
     */
    public void maybeInjectFault(String correlationId) {
        if (!faultEnabled) {
            return;
        }

        if (random.nextDouble() < faultProbability) {
            log.warn("[Bank] Simulating service failure - correlationId={}", correlationId);
            throw new RuntimeException("Simulated bank service failure");
        }
    }

    public void enableFaults(double probability) {
        this.faultEnabled = true;
        this.faultProbability = probability;
        log.info("[Bank] Fault injection enabled - probability={}", probability);
    }

    public void disableFaults() {
        this.faultEnabled = false;
        log.info("[Bank] Fault injection disabled");
    }

    public boolean isFaultEnabled() {
        return faultEnabled;
    }
}
