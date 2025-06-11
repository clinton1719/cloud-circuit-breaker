package com.cloudcb.core;

import java.time.Instant;

/**
 * Represents the runtime state of a circuit breaker instance.
 * <p>
 * This class holds the status (OPEN/CLOSED), failure count, and timestamp of the last failure.
 * It is used by {@link CircuitBreakerManager} and persisted via {@link com.cloudcb.store.CircuitBreakerStore}.
 * </p>
 *
 * <p>
 * This class is a simple POJO and is not thread-safe by itself. Concurrency concerns should be handled by the store.
 * </p>
 *
 * @author Clinton Fernandes
 */
public class CircuitBreakerState {

    /**
     * The current status of the circuit breaker.
     * Expected values: "CLOSED" or "OPEN".
     */
    public String status;

    /**
     * Number of consecutive failures since the circuit was last closed or reset.
     */
    public int failureCount;

    /**
     * The timestamp of the last recorded failure.
     */
    public Instant lastFailureTime;

    /**
     * Constructs a new {@code CircuitBreakerState}.
     *
     * @param status          The current status ("OPEN" or "CLOSED").
     * @param failureCount    The number of consecutive failures.
     * @param lastFailureTime The time when the last failure occurred.
     */
    public CircuitBreakerState(String status, int failureCount, Instant lastFailureTime) {
        this.status = status;
        this.failureCount = failureCount;
        this.lastFailureTime = lastFailureTime;
    }
}
