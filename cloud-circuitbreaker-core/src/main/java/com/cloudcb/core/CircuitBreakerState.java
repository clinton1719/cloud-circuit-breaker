package com.cloudcb.core;

import java.time.Instant;

/**
 * @author Clinton Fernandes
 */
public class CircuitBreakerState {
    public String status;
    public int failureCount;
    public Instant lastFailureTime;

    public CircuitBreakerState(String status, int failureCount, Instant lastFailureTime) {
        this.status = status;
        this.failureCount = failureCount;
        this.lastFailureTime = lastFailureTime;
    }
}
