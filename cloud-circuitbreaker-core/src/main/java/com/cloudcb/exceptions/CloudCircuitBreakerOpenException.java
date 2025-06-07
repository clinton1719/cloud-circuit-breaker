package com.cloudcb.exceptions;

import java.util.Map;

/**
 * Exception thrown when a circuit breaker is in an open state, indicating that the system should not attempt
 * the protected operation.
 *
 * <p>
 * This exception is typically thrown by the circuit breaker engine when a method annotated with
 * {@code @CloudCircuitBreaker} or wrapped via {@code CloudCircuitBreakerEngine} is attempted while the circuit
 * is open.
 * </p>
 *
 * <p>
 * Additional metadata can be attached to the exception to aid with logging, tracing, or debugging.
 * </p>
 *
 * <p><b>Example usage:</b></p>
 * <pre>{@code
 * throw new CloudCircuitBreakerOpenException("Circuit is open", Map.of("Function", "getUser", "Region", "us-east-1"));
 * }</pre>
 *
 * @author Clinton Fernandes
 */
public class CloudCircuitBreakerOpenException extends RuntimeException {

    /**
     * Optional metadata to provide context about the exception (e.g., function name, service, region).
     */
    private final Map<String, String> metadata;

    /**
     * Constructs a new exception with the specified message and metadata.
     *
     * @param message  the detail message
     * @param metadata additional metadata to attach to the exception (may be null or empty)
     */
    public CloudCircuitBreakerOpenException(String message, Map<String, String> metadata) {
        super(message);
        this.metadata = metadata;
    }

    /**
     * Returns the metadata associated with this exception, if any.
     *
     * @return a map of metadata key-value pairs (may be empty but never null)
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Returns the detail message string of this exception, augmented with metadata if present.
     *
     * @return the detail message with metadata appended
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        if (metadata != null && !metadata.isEmpty()) {
            sb.append(" | Metadata: ").append(metadata);
        }
        return sb.toString();
    }
}
