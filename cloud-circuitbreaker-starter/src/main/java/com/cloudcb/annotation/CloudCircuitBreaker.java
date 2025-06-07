package com.cloudcb.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a method as circuit-breaker protected.
 * <p>
 * When applied, the method execution is intercepted and routed through the CloudCircuitBreaker engine.
 * This enables automatic failure tracking, circuit opening, and optional fallback mechanisms.
 *
 * <p>
 * The circuit state is managed externally via the configured {@link com.cloudcb.store.CircuitBreakerStore},
 * typically backed by a distributed store like DynamoDB.
 *
 * <p><b>Usage example:</b>
 * <pre>
 * {@code
 * @CloudCircuitBreaker(function = "fetchUserData", fallback = "fallbackMethod")
 * public String fetchData() {
 *     // potentially unreliable code
 * }
 *
 * public String fallbackMethod() {
 *     return "default response";
 * }
 * }
 * </pre>
 *
 * @author Clinton Fernandes
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CloudCircuitBreaker {

    /**
     * A unique function identifier to track circuit breaker metrics.
     * If left empty, the method name is used as the key.
     *
     * @return the logical name used in the circuit breaker registry/store
     */
    String function() default "";

    /**
     * The name of a fallback method to invoke if the circuit is open or an exception is thrown.
     * The fallback method must exist in the same class, match the signature, and be accessible.
     *
     * @return the name of the fallback method
     */
    String fallback() default "";
}
