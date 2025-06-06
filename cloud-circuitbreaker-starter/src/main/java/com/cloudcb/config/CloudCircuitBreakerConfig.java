package com.cloudcb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Clinton Fernandes
 */
@ConfigurationProperties(prefix = "cloudcb")
public class CloudCircuitBreakerConfig {

    private static CloudCircuitBreakerConfig instance;
    private String serviceName;
    private String tableName;

    public static void init(String service, String table) {
        CloudCircuitBreakerConfig config = new CloudCircuitBreakerConfig();
        config.setServiceName(service);
        config.setTableName(table);
        instance = config;
    }

    public static String getServiceNameStatic() {
        if (instance != null && instance.serviceName != null) return instance.serviceName;
        return System.getenv("CLOUDCB_SERVICE");
    }

    public static String getTableNameStatic() {
        if (instance != null && instance.tableName != null) return instance.tableName;
        return System.getenv("CLOUDCB_TABLE");
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
