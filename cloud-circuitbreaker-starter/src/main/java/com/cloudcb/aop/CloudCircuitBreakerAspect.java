package com.cloudcb.aop;

import com.cloudcb.annotation.CloudCircuitBreaker;
import com.cloudcb.config.CloudCircuitBreakerConfig;
import com.cloudcb.core.CircuitBreakerManager;
import com.cloudcb.exceptions.CloudCircuitBreakerOpenException;
import com.cloudcb.store.CircuitBreakerStore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Aspect for applying circuit breaker behavior to methods annotated with {@link com.cloudcb.annotation.CloudCircuitBreaker}.
 *
 * <p>
 * This AOP-based component intercepts method executions and wraps them with circuit breaker logic.
 * It uses {@link CircuitBreakerManager} to determine if a circuit is open for a given function and
 * prevents further calls when necessary. Optionally, a fallback method can be invoked when the circuit is open.
 * </p>
 *
 * <p>
 * The circuit key is derived from the service name (as configured via {@link CloudCircuitBreakerConfig})
 * and the method name (or the explicitly provided function name via annotation).
 * </p>
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * @CloudCircuitBreaker(function = "placeOrder", fallback = "handleFailure")
 * public OrderResponse placeOrder(OrderRequest request) {
 *     // method logic
 * }
 *
 * public OrderResponse handleFailure(OrderRequest request) {
 *     // fallback logic
 * }
 * }</pre>
 *
 * <p>The fallback method must:</p>
 *
 * <ul>
 *   <li>Be in the same class as the annotated method</li>
 *   <li>Have the exact same method signature</li>
 * </ul>
 *
 * @author Clinton Fernandes
 */
@Aspect
@Component
public class CloudCircuitBreakerAspect {
    private final CircuitBreakerManager circuitBreakerManager;

    private final CloudCircuitBreakerConfig config;

    /**
     * Constructs the aspect with the provided store, which is used to manage circuit breaker state.
     *
     * @param store  The implementation of {@link CircuitBreakerStore} to use for persistence
     * @param config Config required for different arguments
     */
    public CloudCircuitBreakerAspect(CircuitBreakerStore store, CloudCircuitBreakerConfig config) {
        this.circuitBreakerManager = new CircuitBreakerManager(store);
        this.config = config;
    }

    /**
     * Intercepts method execution and applies circuit breaker logic based on {@link CloudCircuitBreaker} annotation.
     *
     * <p>If the circuit is open, and a fallback is defined, the fallback method is invoked.
     * If the fallback is missing or fails, a {@link CloudCircuitBreakerOpenException} is thrown.</p>
     *
     * @param joinPoint The join point representing the intercepted method
     * @return The result of the original method or the fallback
     * @throws Throwable if the circuit is open and no fallback is provided, or if execution/fallback fails
     */
    @Around("@annotation(com.cloudcb.annotation.CloudCircuitBreaker)")
    public Object wrapWithCircuitBreaker(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        CloudCircuitBreaker annotation = method.getAnnotation(CloudCircuitBreaker.class);

        String functionName = annotation.function().isEmpty() ? method.getName() : annotation.function();
        String serviceName = config.getServiceName();
        if (serviceName == null)
            throw new IllegalStateException("CloudCircuitBreakerConfig.serviceName is not configured. Please set 'cloudcb.service-name' in application.properties or application.yml.");

        String cbKey = serviceName + ":" + functionName;

        if (circuitBreakerManager.isCircuitOpen(cbKey)) {
            String fallbackMethodName = annotation.fallback();
            if (!fallbackMethodName.isEmpty()) {
                try {
                    Method fallbackMethod = joinPoint.getTarget().getClass().getMethod(fallbackMethodName, method.getParameterTypes());
                    return fallbackMethod.invoke(joinPoint.getTarget(), joinPoint.getArgs());
                } catch (Exception e) {
                    throw new IllegalStateException(String.format("Circuit is OPEN and fallback failed for: {}", fallbackMethodName), e);
                }
            }
            throw new CloudCircuitBreakerOpenException("Circuit is OPEN and fallback failed", Map.of("cbKey", cbKey));
        }

        try {
            Object result = joinPoint.proceed();
            circuitBreakerManager.recordSuccess(cbKey);
            return result;
        } catch (Throwable ex) {
            circuitBreakerManager.recordFailure(cbKey);
            throw ex;
        }
    }
}
