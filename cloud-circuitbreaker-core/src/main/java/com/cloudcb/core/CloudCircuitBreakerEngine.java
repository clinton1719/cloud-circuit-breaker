package com.cloudcb.core;

import java.util.concurrent.Callable;

/**
 * @author Clinton Fernandes
 */

public class CloudCircuitBreakerEngine {

    private final CircuitBreakerManager manager;

    public CloudCircuitBreakerEngine(CircuitBreakerManager manager) {
        this.manager = manager;
    }

    public <T> T execute(String functionName, Callable<T> callable) throws Exception {
        if (manager.isCircuitOpen(functionName)) {
            throw new RuntimeException("Circuit is OPEN for: " + functionName);
        }

        try {
            T result = callable.call();
            manager.recordSuccess(functionName);
            return result;
        } catch (Exception ex) {
            manager.recordFailure(functionName);
            throw ex;
        }
    }
}