package com.parkhere.configuration.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BulkDataInsertion {

    private static final String TABLE_NAME = "ParkingLots";
    private static final String FILE_PATH = "data.json";
    private static final int BATCH_SIZE = 25;

    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            List<Map<String, AttributeValue>> items = readItemsFromFile(FILE_PATH);
            if (!items.isEmpty()) {
                bulkInsert(items);
            } else {
                log.warn("No items found in {}", FILE_PATH);
            }
        };
    }

    private List<Map<String, AttributeValue>> readItemsFromFile(String filePath) {
        try {
            List<Map<String, Object>> rawItems = objectMapper.readValue(
                    new File(filePath),
                    new TypeReference<>() {}
            );

            List<Map<String, AttributeValue>> items = new ArrayList<>();
            for (Map<String, Object> rawItem : rawItems) {
                items.add(convertToAttributeMap(rawItem));
            }
            return items;
        } catch (IOException e) {
            log.error("Error reading file: {}", filePath, e);
            return Collections.emptyList();
        }
    }

    private Map<String, AttributeValue> convertToAttributeMap(Map<String, Object> rawItem) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", toAttributeValue(rawItem.get("id")));
        item.put("parkingLotId", toAttributeValue(rawItem.get("parkingLotId")));
        item.put("spotId", toAttributeValue(rawItem.get("spotId")));
        item.put("spotName", AttributeValue.builder().s((String) rawItem.get("spotName")).build());
        item.put("priority", toAttributeValue(rawItem.get("priority")));
        return item;
    }

    private AttributeValue toAttributeValue(Object value) {
        return value != null ? AttributeValue.builder().n(value.toString()).build() : AttributeValue.builder().nul(true).build();
    }

    private void bulkInsert(List<Map<String, AttributeValue>> items) {
        try {
            for (int i = 0; i < items.size(); i += BATCH_SIZE) {
                List<Map<String, AttributeValue>> batch = items.subList(i, Math.min(i + BATCH_SIZE, items.size()));
                List<WriteRequest> writeRequests = new ArrayList<>();
                for (Map<String, AttributeValue> item : batch) {
                    writeRequests.add(WriteRequest.builder()
                            .putRequest(PutRequest.builder().item(item).build())
                            .build());
                }
                BatchWriteItemRequest batchRequest = BatchWriteItemRequest.builder()
                        .requestItems(Map.of(TABLE_NAME, writeRequests))
                        .build();
                BatchWriteItemResponse response = dynamoDbClient.batchWriteItem(batchRequest);
                if (!response.unprocessedItems().isEmpty()) {
                    log.warn("Unprocessed items: {}", response.unprocessedItems());
                } else {
                    log.info("Batch {} inserted successfully.", (i / BATCH_SIZE) + 1);
                }
            }
        } catch (DynamoDbException e) {
            log.error("Error inserting items into DynamoDB", e);
        }
    }
}
