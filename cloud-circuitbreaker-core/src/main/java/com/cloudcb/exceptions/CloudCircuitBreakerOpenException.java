package com.cloudcb.exceptions;

import java.util.Map;

/**
 * @author Clinton Fernandes
 */
public class CloudCircuitBreakerOpenException extends RuntimeException {
    private final Map<String, String> metadata;

    public CloudCircuitBreakerOpenException(String message, Map<String, String> metadata) {
        super(message);
        this.metadata = metadata;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (metadata != null && !metadata.isEmpty()) {
            sb.append(" | Metadata: ").append(metadata);
        }
        return sb.toString();
    }
}
