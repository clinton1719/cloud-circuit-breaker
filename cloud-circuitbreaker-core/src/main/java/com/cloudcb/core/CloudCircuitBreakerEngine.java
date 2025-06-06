package com.cloudcb.core;

import com.cloudcb.exceptions.CloudCircuitBreakerOpenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Clinton Fernandes
 */

public class CloudCircuitBreakerEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudCircuitBreakerEngine.class);

    private final CircuitBreakerManager manager;

    public CloudCircuitBreakerEngine(CircuitBreakerManager manager) {
        this.manager = manager;
    }

    public <T> T execute(String functionName, Callable<T> callable) throws Exception {
        if (manager.isCircuitOpen(functionName)) {
            LOGGER.error("Circuit is OPEN");
            throw new CloudCircuitBreakerOpenException("Circuit is OPEN", Map.of("Function", functionName));
        }

        try {
            T result = callable.call();
            manager.recordSuccess(functionName);
            return result;
        } catch (Exception ex) {
            manager.recordFailure(functionName);
            LOGGER.error(ex.getMessage());
            throw ex;
        }
    }
}