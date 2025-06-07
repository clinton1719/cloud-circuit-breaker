package com.cloudcb.config;

import com.cloudcb.aop.CloudCircuitBreakerAspect;
import com.cloudcb.core.CircuitBreakerManager;
import com.cloudcb.store.CircuitBreakerStore;
import com.cloudcb.store.impl.DynamoDBCircuitBreakerStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Auto-configuration class for setting up the Cloud Circuit Breaker components.
 *
 * <p>
 * This class registers beans required for circuit breaker functionality,
 * such as {@link CircuitBreakerStore}, {@link CircuitBreakerManager}, and the AOP {@link CloudCircuitBreakerAspect}.
 * </p>
 *
 * <p>
 * It conditionally provides a {@link DynamoDBCircuitBreakerStore} as the default store implementation
 * if the configured table type is {@code dynamodb}.
 * Other store types can be added in the future via similar condition-based logic.
 * </p>
 *
 * <p>
 * To customize this configuration, override the relevant beans in your application context,
 * or supply properties via {@code application.yml} or {@code application.properties}:
 * </p>
 *
 * <pre>
 * cloudcb:
 *   tableType: dynamodb
 *   region: ap-south-1
 *   tableName: cloud-cb-table
 *   serviceName: order-service
 * </pre>
 *
 * <p>
 * This class is loaded automatically via Spring Boot's {@code @AutoConfiguration} mechanism.
 * </p>
 *
 * @author Clinton Fernandes
 */
@AutoConfiguration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(CloudCircuitBreakerConfig.class)
public class CloudCircuitBreakerAutoConfiguration {

    /**
     * Default constructor for CloudCircuitBreakerConfig.
     */
    public CloudCircuitBreakerAutoConfiguration(){

    }

    /**
     * Creates a {@link CircuitBreakerStore} based on the {@code cloudcb.tableType} configuration property.
     *
     * <p>If no other {@link CircuitBreakerStore} is defined in the context,
     * a {@link DynamoDBCircuitBreakerStore} will be created using the provided AWS region and table name.</p>
     *
     * @param config the circuit breaker configuration properties
     * @return a store implementation based on the selected backend
     * @throws IllegalArgumentException for missing arguments
     */
    @Bean
    @ConditionalOnMissingBean(CircuitBreakerStore.class)
    public CircuitBreakerStore circuitBreakerStore(CloudCircuitBreakerConfig config) {
        String type = config.getTableType();
        if ("dynamodb".equalsIgnoreCase(type)) {
            if (config.getRegion() == null) {
                throw new IllegalArgumentException("Please specify region for DynamoDB client");
            }
            DynamoDbClient dynamoDbClient = DynamoDbClient.builder().region(Region.of(config.getRegion())).build();
            return new DynamoDBCircuitBreakerStore(dynamoDbClient, config.getTableName());
        } else {
            throw new IllegalArgumentException(String.format("Invalid circuit breaker store type: '%s'. Supported types: [dynamodb]. " + "Please check the 'cloudcb.tableType' property in your application configuration.", type == null ? "null" : type.trim()));
        }
    }

    /**
     * Registers a {@link CircuitBreakerManager} if one is not already defined in the context.
     *
     * @param store  the store implementation to use for tracking circuit state
     * @param config the circuit breaker configuration
     * @return a configured circuit breaker manager
     */
    @Bean
    @ConditionalOnMissingBean(CircuitBreakerManager.class)
    public CircuitBreakerManager circuitBreakerManager(CircuitBreakerStore store, CloudCircuitBreakerConfig config) {
        return new CircuitBreakerManager(store);
    }

    /**
     * Registers the AOP aspect that applies circuit breaker logic to annotated methods.
     *
     * @param store the store used to track circuit state
     * @return the circuit breaker aspect
     */
    @Bean
    @ConditionalOnMissingBean(CloudCircuitBreakerAspect.class)
    public CloudCircuitBreakerAspect cloudCircuitBreakerAspect(CircuitBreakerStore store) {
        return new CloudCircuitBreakerAspect(store);
    }
}

