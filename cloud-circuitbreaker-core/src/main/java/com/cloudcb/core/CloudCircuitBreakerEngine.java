package com.cloudcb.core;

import com.cloudcb.exceptions.CloudCircuitBreakerOpenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Core execution engine for circuit breaker-protected operations.
 * <p>
 * This class wraps a callable execution with circuit breaker logic,
 * preventing calls from being made if the associated circuit is open.
 * </p>
 *
 * <p>
 * If the circuit is open for the given function name, an exception is thrown immediately.
 * Otherwise, the callable is executed and circuit state is updated based on the result.
 * </p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * CloudCircuitBreakerEngine engine = new CloudCircuitBreakerEngine(manager);
 * String result = engine.execute("orderService.createOrder", () -> callRemoteService());
 * }</pre>
 *
 * @author Clinton Fernandes
 */
public class CloudCircuitBreakerEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloudCircuitBreakerEngine.class);

    private final CircuitBreakerManager manager;

    /**
     * Constructs a new circuit breaker execution engine with the provided manager.
     *
     * @param manager The {@link CircuitBreakerManager} that controls state transitions and storage.
     */
    public CloudCircuitBreakerEngine(CircuitBreakerManager manager) {
        this.manager = manager;
    }

    /**
     * Executes a callable if the circuit for the given function name is closed.
     * <p>
     * If the circuit is open, throws a {@link CloudCircuitBreakerOpenException} immediately.
     * After execution:
     * <ul>
     *   <li>On success: resets the failure count (closes the circuit if needed).</li>
     *   <li>On failure: increments failure count and opens the circuit if threshold is exceeded.</li>
     * </ul>
     *
     * @param functionName A unique identifier for the protected function (e.g., "billingService.chargeCard").
     * @param callable     The business logic to execute.
     * @param <T>          The return type of the callable.
     * @return The result of the callable if successful.
     * @throws CloudCircuitBreakerOpenException If the circuit is currently open for this function.
     * @throws Exception                        If the callable throws any exception.
     */
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
