package com.parkhere.configuration.service;

import com.parkhere.configuration.exception.ConfigServiceException;
import com.parkhere.configuration.model.ParkingSpot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigurationService {

    private static final String TABLE_NAME = "ParkingLots";
    private static final String INDEX_NAME = "ParkingLotIdIndex";
    private static final String ERROR_MESSAGE = "Failed to query parking spots from DynamoDB";

    private final DynamoDbClient dynamoDbClient;

    public List<ParkingSpot> getParkingSpots(int parkingLotId) {
        try {
            QueryRequest queryRequest = buildQueryRequest(parkingLotId);
            QueryResponse queryResponse = dynamoDbClient.query(queryRequest);
            return queryResponse.items().stream()
                    .map(this::mapToParkingSpot)
                    .toList();
        } catch (DynamoDbException e) {
            log.error("{} for parkingLotId={}", ERROR_MESSAGE, parkingLotId, e);
            throw new ConfigServiceException(ERROR_MESSAGE);
        }
    }

    private QueryRequest buildQueryRequest(int parkingLotId) {
        return QueryRequest.builder()
                .tableName(TABLE_NAME)
                .indexName(INDEX_NAME)
                .keyConditionExpression("parkingLotId = :parkingLotId")
                .expressionAttributeValues(Map.of(
                        ":parkingLotId", AttributeValue.builder().n(Integer.toString(parkingLotId)).build()
                ))
                .build();
    }

    private ParkingSpot mapToParkingSpot(Map<String, AttributeValue> item) {
        return new ParkingSpot(
                parseIntAttribute(item.get("spotId")),
                Optional.ofNullable(item.get("spotName")).map(AttributeValue::s).orElse("Unknown"),
                parseIntAttribute(item.get("priority"))
        );
    }

    private int parseIntAttribute(AttributeValue attributeValue) {
        return Optional.ofNullable(attributeValue)
                .map(AttributeValue::n)
                .map(Integer::parseInt)
                .orElse(0);
    }
}
