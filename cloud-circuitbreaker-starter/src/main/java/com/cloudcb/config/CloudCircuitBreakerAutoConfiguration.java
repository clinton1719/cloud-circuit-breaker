package com.cloudcb.config;

import com.cloudcb.aop.CloudCircuitBreakerAspect;
import com.cloudcb.core.CircuitBreakerManager;
import com.cloudcb.store.CircuitBreakerStore;
import com.cloudcb.store.impl.DynamoDBCircuitBreakerStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Clinton Fernandes
 */
@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(CloudCircuitBreakerConfig.class)
public class CloudCircuitBreakerAutoConfiguration {

    @Bean// Only create if no other CircuitBreakerStore bean exists
    public CircuitBreakerStore circuitBreakerStore(CloudCircuitBreakerConfig config) {
        // Implement your default store (e.g., in-memory or DynamoDB using config.getTableName())
        // For a starter, you might provide a simple in-memory store by default
        // Or conditionally provide a DynamoDB store if aws-sdk-dynamodb is on classpath
        return new DynamoDBCircuitBreakerStore(null, null); // Placeholder
    }

    @Bean
    public CircuitBreakerManager circuitBreakerManager(CircuitBreakerStore store, CloudCircuitBreakerConfig config) {
        return new CircuitBreakerManager(store);
    }

    @Bean
    public CloudCircuitBreakerAspect cloudCircuitBreakerAspect(CircuitBreakerStore store) {
        return new CloudCircuitBreakerAspect(store);
    }
}
