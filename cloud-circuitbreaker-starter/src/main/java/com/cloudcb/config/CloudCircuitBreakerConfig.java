package com.cloudcb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the Cloud Circuit Breaker library.
 *
 * <p>
 * This class maps properties prefixed with <code>cloudcb</code> from
 * {@code application.properties} or {@code application.yml}, allowing users to configure:
 * </p>
 *
 * <ul>
 *     <li>Service name (used in circuit breaker keys)</li>
 *     <li>DynamoDB table name and type</li>
 *     <li>AWS region for the backend store</li>
 * </ul>
 *
 * <p>
 * It also provides static fallback methods to retrieve configuration from environment variables,
 * enabling compatibility with environments like AWS Lambda where Spring context may not be used directly.
 * </p>
 *
 * <p>
 * Example configuration:
 * </p>
 *
 * <pre>{@code
 * cloudcb.service-name=order-service
 * cloudcb.table-name=cloud-cb-table
 * cloudcb.table-type=dynamodb
 * cloudcb.region=ap-south-1
 * }</pre>
 *
 * <p>
 * Static initialization is supported via {@link #init(String, String, String, String, String, String)} (String, String, String, String)},
 * typically useful for non-Spring runtimes.
 * </p>
 *
 * @author Clinton Fernandes
 */
@ConfigurationProperties(prefix = "cloudcb")
@Component
public class CloudCircuitBreakerConfig {

    private static CloudCircuitBreakerConfig instance;
    private String serviceName;
    private String tableName;
    private String tableType;
    private String region;

    private String failureThreshold;

    private String resetTimeoutSeconds;


    /**
     * Default constructor for CloudCircuitBreakerConfig.
     */
    public CloudCircuitBreakerConfig() {
    }

    /**
     * Initializes a static singleton instance with manually provided values.
     * Useful in contexts outside Spring (e.g., AWS Lambda).
     *
     * @param service             the service name
     * @param table               the DynamoDB table name
     * @param tableType           the backing store type (e.g., dynamodb)
     * @param region              the AWS region
     * @param failureThreshold    the threshold for breakpoint
     * @param resetTimeoutSeconds the timeout for reset
     */
    public static void init(String service, String table, String tableType, String region, String failureThreshold, String resetTimeoutSeconds) {
        CloudCircuitBreakerConfig config = new CloudCircuitBreakerConfig();
        config.setServiceName(service);
        config.setTableName(table);
        config.setTableType(tableType);
        config.setRegion(region);
        config.setFailureThreshold(failureThreshold);
        config.setResetTimeoutSeconds(resetTimeoutSeconds);
        instance = config;
    }

    /**
     * Returns the configured service name, or falls back to {@code CLOUDCB_SERVICE} environment variable.
     *
     * @return service name
     */
    public static String getServiceNameStatic() {
        if (instance != null && instance.serviceName != null) return instance.serviceName;
        return System.getenv("CLOUDCB_SERVICE");
    }

    /**
     * Returns the configured table name, or falls back to {@code CLOUDCB_TABLE_NAME} environment variable.
     *
     * @return DynamoDB table name
     */
    public static String getTableNameStatic() {
        if (instance != null && instance.tableName != null) return instance.tableName;
        return System.getenv("CLOUDCB_TABLE_NAME");
    }

    /**
     * Returns the configured table type, or falls back to {@code CLOUDCB_TABLE_TYPE} environment variable.
     *
     * @return the type of circuit breaker store (e.g., dynamodb)
     */
    public static String getTableTypeStatic() {
        if (instance != null && instance.tableType != null) return instance.tableType;
        return System.getenv("CLOUDCB_TABLE_TYPE");
    }

    /**
     * Returns the configured AWS region, or falls back to {@code CLOUDCB_TABLE_REGION} environment variable.
     *
     * @return AWS region
     */
    public static String getTableRegionStatic() {
        if (instance != null && instance.region != null) return instance.region;
        return System.getenv("CLOUDCB_TABLE_REGION");
    }

    /**
     * Returns the configured service name used in circuit breaker keys.
     *
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Sets the service name used in circuit breaker keys.
     *
     * @param serviceName the service name to set
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Returns the name of the DynamoDB table used to persist circuit breaker state.
     *
     * @return the DynamoDB table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the name of the DynamoDB table used to persist circuit breaker state.
     *
     * @param tableName the DynamoDB table name to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Returns the type of the backend store used by the circuit breaker (e.g., "dynamodb").
     *
     * @return the backend store type
     */
    public String getTableType() {
        return tableType;
    }

    /**
     * Sets the type of the backend store used by the circuit breaker (e.g., "dynamodb").
     *
     * @param tableType the backend store type to set
     */
    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    /**
     * Returns the AWS region where the backend store (e.g., DynamoDB) is located.
     *
     * @return the AWS region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the AWS region where the backend store (e.g., DynamoDB) is located.
     *
     * @param region the AWS region to set
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Gets the failure threshold after which circuit breaks
     *
     * @return the threshold limit
     */
    public String getFailureThreshold() {
        return failureThreshold;
    }

    /**
     * Sets the failure threshold after which circuit breaks
     *
     * @param failureThreshold the threshold for breakpoint
     */
    public void setFailureThreshold(String failureThreshold) {
        this.failureThreshold = failureThreshold;
    }

    /**
     * Gets the timeout in seconds after which circuit resets
     *
     * @return the threshold limit
     */
    public String getResetTimeoutSeconds() {
        return resetTimeoutSeconds;
    }

    /**
     * Sets the timeout in seconds after which circuit resets
     *
     * @param resetTimeoutSeconds the timeout in seconds for reset
     */
    public void setResetTimeoutSeconds(String resetTimeoutSeconds) {
        this.resetTimeoutSeconds = resetTimeoutSeconds;
    }
}
