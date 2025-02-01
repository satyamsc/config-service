package com.parkhere.configuration.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;



@Slf4j
@Configuration
public class AwsConfig {
    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.create();
    }
}