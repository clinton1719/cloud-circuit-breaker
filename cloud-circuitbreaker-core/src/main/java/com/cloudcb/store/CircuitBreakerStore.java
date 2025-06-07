package com.cloudcb.store;

import com.cloudcb.core.CircuitBreakerState;

/**
 * Interface for persisting and retrieving the state of a circuit breaker.
 * <p>
 * Implementations of this interface are responsible for providing a durable store
 * (e.g., DynamoDB, Redis, in-memory) for circuit breaker states identified by a unique key.
 * </p>
 * <p>
 * This allows for distributed or clustered systems to share circuit breaker status across instances.
 * </p>
 *
 * @author Clinton Fernandes
 */
public interface CircuitBreakerStore {

    /**
     * Retrieves the current state of the circuit breaker for the given key.
     *
     * @param key A unique identifier representing a specific circuit breaker (e.g., service.method).
     * @return The current {@link CircuitBreakerState}, or {@code null} if no state exists.
     */
    CircuitBreakerState getState(String key);

    /**
     * Persists the circuit breaker state for the given key.
     *
     * @param key   A unique identifier representing a specific circuit breaker.
     * @param state The {@link CircuitBreakerState} to persist.
     */
    void saveState(String key, CircuitBreakerState state);

    /**
     * Resets (removes or reinitializes) the circuit breaker state for the given key.
     * This typically moves the circuit breaker back to the initial (closed) state.
     *
     * @param key A unique identifier representing a specific circuit breaker.
     */
    void reset(String key);
}
