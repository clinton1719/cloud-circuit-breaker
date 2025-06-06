package com.cloudcb.core;

import com.cloudcb.store.CircuitBreakerStore;

import java.time.Instant;

/**
 * @author Clinton Fernandes
 */
public class CircuitBreakerManager {
    private final CircuitBreakerStore store;
    private static final int FAILURE_THRESHOLD = 5;
    private static final int RESET_TIMEOUT_SECONDS = 30;
    public CircuitBreakerManager(CircuitBreakerStore store) {
        this.store = store;
    }

    public boolean isCircuitOpen(String key) {
        CircuitBreakerState state = store.getState(key);
        if (state == null) return false;

        if ("OPEN".equals(state.status)) {
            Instant now = Instant.now();
            if (now.isAfter(state.lastFailureTime.plusSeconds(RESET_TIMEOUT_SECONDS))) {
                return false;
            }
            return true;
        }
        return false;
    }

    public void recordSuccess(String key) {
        store.reset(key);
    }

    public void recordFailure(String key) {
        CircuitBreakerState state = store.getState(key);
        if (state == null) {
            state = new CircuitBreakerState("CLOSED", 1, Instant.now());
        } else {
            state.failureCount++;
            state.lastFailureTime = Instant.now();
            if (state.failureCount >= FAILURE_THRESHOLD) {
                state.status = "OPEN";
            }
        }
        store.saveState(key, state);
    }

}
