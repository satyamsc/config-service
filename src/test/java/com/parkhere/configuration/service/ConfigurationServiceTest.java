package com.parkhere.configuration.service;

import com.parkhere.configuration.exception.ConfigServiceException;
import com.parkhere.configuration.model.ParkingSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConfigurationServiceTest {

    @Mock
    private DynamoDbClient dynamoDbClient;

    @InjectMocks
    private ConfigurationService configurationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetParkingSpots_Success() {
        QueryResponse queryResponse = mock(QueryResponse.class);
        when(queryResponse.items()).thenReturn(List.of(
                Map.of(
                        "spotId", AttributeValue.builder().n("101").build(),
                        "spotName", AttributeValue.builder().s("VIP Spot").build(),
                        "priority", AttributeValue.builder().n("1").build()
                )
        ));
        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(queryResponse);
        List<ParkingSpot> result = configurationService.getParkingSpots(1);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(101, result.get(0).getId());
        assertEquals("VIP Spot", result.get(0).getSpotName());
        assertEquals(1, result.get(0).getPriority());

        verify(dynamoDbClient, times(1)).query(any(QueryRequest.class));
    }

    @Test
    void testGetParkingSpots_NoResults() {
        // Arrange
        QueryResponse queryResponse = mock(QueryResponse.class);
        when(queryResponse.items()).thenReturn(List.of());
        when(dynamoDbClient.query(any(QueryRequest.class))).thenReturn(queryResponse);
        List<ParkingSpot> result = configurationService.getParkingSpots(2);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dynamoDbClient, times(1)).query(any(QueryRequest.class));
    }

    @Test
    void testGetParkingSpots_DynamoDbException() {
        when(dynamoDbClient.query(any(QueryRequest.class)))
                .thenThrow(DynamoDbException.builder().message("DynamoDB error").build());

        ConfigServiceException exception = assertThrows(ConfigServiceException.class,
                () -> configurationService.getParkingSpots(3));
        assertEquals("Failed to query parking spots from DynamoDB", exception.getMessage());
        verify(dynamoDbClient, times(1)).query(any(QueryRequest.class));
    }
}
