package com.cloudcb.aop;

import com.cloudcb.annotation.CloudCircuitBreaker;
import com.cloudcb.config.CloudCircuitBreakerConfig;
import com.cloudcb.core.CircuitBreakerManager;
import com.cloudcb.store.CircuitBreakerStore;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author Clinton Fernandes
 */
@Aspect
@Component
public class CloudCircuitBreakerAspect {
    private final CircuitBreakerManager circuitBreakerManager;

    public CloudCircuitBreakerAspect(CircuitBreakerStore store) {
        this.circuitBreakerManager = new CircuitBreakerManager(store);
    }

    @Around("@annotation(com.cloudcb.annotation.CloudCircuitBreaker)")
    public Object wrapWithCircuitBreaker(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        CloudCircuitBreaker annotation = method.getAnnotation(CloudCircuitBreaker.class);

        String functionName = annotation.function().isEmpty() ? method.getName() : annotation.function();
        String serviceName = CloudCircuitBreakerConfig.getServiceNameStatic();
        String cbKey = serviceName + ":" + functionName;

        if (circuitBreakerManager.isCircuitOpen(cbKey)) {
            String fallbackMethodName = annotation.fallback();
            if (!fallbackMethodName.isEmpty()) {
                try {
                    Method fallbackMethod = joinPoint.getTarget().getClass().getMethod(fallbackMethodName, method.getParameterTypes());
                    return fallbackMethod.invoke(joinPoint.getTarget(), joinPoint.getArgs());
                } catch (Exception e) {
                    throw new IllegalStateException("Circuit is OPEN and fallback failed: " + fallbackMethodName, e);
                }
            }
            throw new IllegalStateException("Circuit is OPEN for " + cbKey);
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
