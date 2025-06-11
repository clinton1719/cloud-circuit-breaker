package com.cloudcb.store.impl;

import com.cloudcb.core.CircuitBreakerState;
import com.cloudcb.store.CircuitBreakerStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * A DynamoDB-based implementation of the {@link CircuitBreakerStore} interface.
 *
 * <p>
 * This class persists circuit breaker state in a DynamoDB table with a primary key of {@code id}.
 * It supports conditional updates to prevent race conditions when updating stale states.
 * </p>
 *
 * <p>Required DynamoDB table schema:</p>
 * <ul>
 *     <li>Partition key: {@code id} (String)</li>
 *     <li>Attributes: {@code status} (String), {@code failureCount} (Number), {@code lastFailureTime} (Number - epoch seconds)</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * DynamoDbClient client = DynamoDbClient.create();
 * CircuitBreakerStore store = new DynamoDBCircuitBreakerStore(client, "cloud-cb-table");
 * }</pre>
 *
 * @author Clinton Fernandes
 */
public class DynamoDBCircuitBreakerStore implements CircuitBreakerStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBCircuitBreakerStore.class);

    private final DynamoDbClient dynamoDb;
    private final String tableName;

    /**
     * Constructs a new DynamoDB-backed circuit breaker store.
     *
     * @param dynamoDb  The {@link DynamoDbClient} instance used for accessing DynamoDB.
     * @param tableName The name of the DynamoDB table to store circuit breaker state.
     */
    public DynamoDBCircuitBreakerStore(DynamoDbClient dynamoDb, String tableName) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
    }

    /**
     * Fetches the current state of the circuit breaker from DynamoDB.
     *
     * @param key The unique identifier for the circuit breaker (e.g., "inventoryService.reserveStock").
     * @return The {@link CircuitBreakerState} if present, otherwise {@code null}.
     */
    @Override
    public CircuitBreakerState getState(String key) {
        GetItemRequest request = GetItemRequest.builder().tableName(tableName).key(Map.of("id", AttributeValue.fromS(key))).build();

        Map<String, AttributeValue> item = dynamoDb.getItem(request).item();
        if (item == null || item.isEmpty()) return null;

        String status = item.get("status").s();
        int failureCount = Integer.parseInt(item.get("failureCount").n());
        Instant lastFailureTime = Instant.ofEpochSecond(Long.parseLong(item.get("lastFailureTime").n()));

        return new CircuitBreakerState(status, failureCount, lastFailureTime);
    }

    /**
     * Saves or updates the circuit breaker state in DynamoDB.
     * <p>
     * Uses a conditional expression to prevent overwriting newer states
     * in the event of race conditions or out-of-order writes.
     * </p>
     *
     * @param key   The unique identifier for the circuit breaker.
     * @param state The current {@link CircuitBreakerState} to store.
     */
    @Override
    public void saveState(String key, CircuitBreakerState state) {
        Map<String, AttributeValue> keyMap = Map.of("id", AttributeValue.fromS(key));
        String updateExpression = "SET #status = :statusVal, #failureCount = :failureCountVal, #lastFailureTime = :lastFailureTimeVal";

        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#status", "status");
        expressionAttributeNames.put("#failureCount", "failureCount");
        expressionAttributeNames.put("#lastFailureTime", "lastFailureTime");

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":statusVal", AttributeValue.fromS(state.status));
        expressionAttributeValues.put(":failureCountVal", AttributeValue.fromN(Integer.toString(state.failureCount)));
        expressionAttributeValues.put(":lastFailureTimeVal", AttributeValue.fromN(Long.toString(state.lastFailureTime.getEpochSecond())));

        String conditionExpression = "attribute_not_exists(id) OR #lastFailureTime <= :newTime";
        expressionAttributeValues.put(":newTime", AttributeValue.fromN(Long.toString(state.lastFailureTime.getEpochSecond())));


        try {
            UpdateItemRequest request = UpdateItemRequest.builder().tableName(tableName).key(keyMap).updateExpression(updateExpression).conditionExpression(conditionExpression).expressionAttributeNames(expressionAttributeNames).expressionAttributeValues(expressionAttributeValues).build();

            dynamoDb.updateItem(request);
            LOGGER.debug("Circuit breaker state for key {} saved successfully.", key);
        } catch (ConditionalCheckFailedException e) {
            LOGGER.warn("Skipped outdated CB update for {} due to race conditions or older timestamp. Current update time: {}", key, state.lastFailureTime);
        } catch (DynamoDbException e) {
            LOGGER.error("Error saving circuit breaker state for key {}: {}", key, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            LOGGER.error("An unexpected error occurred while saving circuit breaker state for key {}: {}", key, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Resets the circuit breaker state to default (CLOSED with zero failures).
     *
     * @param key The unique identifier for the circuit breaker.
     */
    @Override
    public void reset(String key) {
        CircuitBreakerState state = new CircuitBreakerState("CLOSED", 0, Instant.now());
        saveState(key, state);
    }
}
