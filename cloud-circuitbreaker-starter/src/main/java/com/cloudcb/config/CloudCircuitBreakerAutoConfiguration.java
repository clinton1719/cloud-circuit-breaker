package com.cloudcb.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author Clinton Fernandes
 */
@AutoConfiguration
@EnableConfigurationProperties(CloudCircuitBreakerConfig.class)
public class CloudCircuitBreakerAutoConfiguration {
}
