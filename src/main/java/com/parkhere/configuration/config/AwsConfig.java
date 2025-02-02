package com.parkhere.configuration.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;



@Slf4j
@Configuration
public class AwsConfig {
    private static final Region AWS_REGION = Region.EU_CENTRAL_1;
    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(AWS_REGION)
                .build();
    }
}