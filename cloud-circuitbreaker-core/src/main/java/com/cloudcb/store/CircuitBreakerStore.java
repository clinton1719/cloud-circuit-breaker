package com.cloudcb.store;

import com.cloudcb.core.CircuitBreakerState;

/**
 * @author Clinton Fernandes
 */
public interface CircuitBreakerStore {
    CircuitBreakerState getState(String key);
    void saveState(String key, CircuitBreakerState state);
    void reset(String key);

}
