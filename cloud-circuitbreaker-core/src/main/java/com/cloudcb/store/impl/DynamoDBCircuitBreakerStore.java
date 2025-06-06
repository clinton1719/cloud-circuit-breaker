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
 * @author Clinton Fernandes
 */
public class DynamoDBCircuitBreakerStore implements CircuitBreakerStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamoDBCircuitBreakerStore.class);
    private final DynamoDbClient dynamoDb;
    private final String tableName;

    public DynamoDBCircuitBreakerStore(DynamoDbClient dynamoDb, String tableName) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
    }

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

    @Override
    public void saveState(String key, CircuitBreakerState state) {
        Map<String, AttributeValue> keyMap = Map.of("id", AttributeValue.fromS(key));

        Map<String, AttributeValueUpdate> updates = new HashMap<>();
        updates.put("status", AttributeValueUpdate.builder().value(AttributeValue.fromS(state.status)).action(AttributeAction.PUT).build());
        updates.put("failureCount", AttributeValueUpdate.builder().value(AttributeValue.fromN(Integer.toString(state.failureCount))).action(AttributeAction.PUT).build());
        updates.put("lastFailureTime", AttributeValueUpdate.builder().value(AttributeValue.fromN(Long.toString(state.lastFailureTime.getEpochSecond()))).action(AttributeAction.PUT).build());

        try {
            UpdateItemRequest request = UpdateItemRequest.builder().tableName(tableName).key(keyMap).attributeUpdates(updates).conditionExpression("attribute_not_exists(id) OR lastFailureTime <= :newTime").expressionAttributeValues(Map.of(":newTime", AttributeValue.fromN(Long.toString(state.lastFailureTime.getEpochSecond())))).build();

            dynamoDb.updateItem(request);
        } catch (ConditionalCheckFailedException e) {
            LOGGER.warn("Skipped outdated CB update for {} due to race conditions", key);
        }
    }

    @Override
    public void reset(String key) {
        CircuitBreakerState state = new CircuitBreakerState("CLOSED", 0, Instant.now());
        saveState(key, state);
    }
}
