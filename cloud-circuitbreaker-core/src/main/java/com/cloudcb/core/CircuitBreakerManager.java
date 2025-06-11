package com.cloudcb.core;

import com.cloudcb.store.CircuitBreakerStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Manages circuit breaker state transitions for a given key using a persistent store.
 * <p>
 * This class tracks failures and determines whether a circuit is open or closed
 * based on a threshold and a reset timeout. It uses a {@link CircuitBreakerStore}
 * to persist state across distributed environments.
 * </p>
 *
 * <p>
 * The circuit opens after failureThreshold consecutive failures
 * and remains open for resetTimeoutSeconds seconds.
 * </p>
 *
 * <p>
 * This class is thread-safe if the underlying {@code CircuitBreakerStore} implementation is thread-safe.
 * </p>
 *
 * @author Clinton Fernandes
 */
public class CircuitBreakerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerManager.class);

    /**
     * The number of failures required to trip the circuit breaker.
     */
    private final int failureThreshold;

    /**
     * Time (in seconds) to wait before allowing a circuit breaker to transition from OPEN to CLOSED again.
     */
    private final int resetTimeoutSeconds;

    private final CircuitBreakerStore store;

    /**
     * Constructs a new {@code CircuitBreakerManager} with the specified persistent store.
     *
     * @param store               The {@link CircuitBreakerStore} implementation for storing circuit breaker state.
     * @param failureThreshold    The failure threshold after which circuit breaks
     * @param resetTimeoutSeconds The time in seconds after which API calls are redirected to original source
     */
    public CircuitBreakerManager(CircuitBreakerStore store, int failureThreshold, int resetTimeoutSeconds) {
        this.store = store;
        this.failureThreshold = failureThreshold;
        this.resetTimeoutSeconds = resetTimeoutSeconds;
    }

    /**
     * Checks if the circuit breaker for the given key is currently open.
     *
     * @param key A unique identifier representing a specific circuit breaker (e.g., "paymentService.charge").
     * @return {@code true} if the circuit is open and within the timeout window; {@code false} otherwise.
     */
    public boolean isCircuitOpen(String key) {
        CircuitBreakerState state = store.getState(key);
        if (state == null) return false;

        if ("OPEN".equals(state.status)) {
            Instant now = Instant.now();
            return !now.isAfter(state.lastFailureTime.plusSeconds(resetTimeoutSeconds));
        }
        return false;
    }

    /**
     * Records a successful operation and resets the circuit breaker state for the given key.
     *
     * @param key A unique identifier representing a specific circuit breaker.
     */
    public void recordSuccess(String key) {
        store.reset(key);
    }

    /**
     * Records a failed operation for the given key.
     * <p>
     * If the failure count exceeds the configured threshold, the circuit will be opened.
     * </p>
     *
     * @param key A unique identifier representing a specific circuit breaker.
     */
    public void recordFailure(String key) {
        CircuitBreakerState state = store.getState(key);
        if (state == null) {
            state = new CircuitBreakerState("CLOSED", 1, Instant.now());
        } else {
            state.failureCount++;
            state.lastFailureTime = Instant.now();
            if (state.failureCount >= failureThreshold) {
                state.status = "OPEN";
                LOGGER.info("Updating state to open for {}", key);
            }
        }
        store.saveState(key, state);
    }
}
