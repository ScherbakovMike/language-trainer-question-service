package com.mikescherbakov.languagetrainerquestionservice.config;

import com.mikescherbakov.languagetrainermodel.course.Course;
import com.mikescherbakov.languagetrainermodel.course.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@RequiredArgsConstructor
@Configuration
public class DynamoDbConfig {

  public static final String QUESTIONS_TABLE_NAME = "questions";
  public static final String QUESTIONS_GSI_NAME = "id-courseId-index";
  public static final String COURSES_TABLE_NAME = "courses";

  @Bean
  public DynamoDbAsyncClient dynamoDbAsyncClient() {
    var region = Region.US_EAST_1;
    return DynamoDbAsyncClient.builder()
        .region(region)
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build();
  }

  @Bean
  public DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient() {
    return DynamoDbEnhancedAsyncClient.builder()
        .dynamoDbClient(dynamoDbAsyncClient())
        .build();
  }

  @Bean
  public DynamoDbAsyncTable<Question> questionTable(DynamoDbEnhancedAsyncClient dynamoDbClient) {
    return dynamoDbClient.table(QUESTIONS_TABLE_NAME, TableSchema.fromBean(Question.class));
  }

  @Bean
  public DynamoDbAsyncIndex<Question> questionGSI(DynamoDbEnhancedAsyncClient dynamoDbClient) {
    return dynamoDbClient
        .table(QUESTIONS_TABLE_NAME, TableSchema.fromBean(Question.class))
        .index(QUESTIONS_GSI_NAME);
  }

  @Bean
  public DynamoDbAsyncTable<Course> courseTable(DynamoDbEnhancedAsyncClient dynamoDbClient) {
    return dynamoDbClient.table(COURSES_TABLE_NAME, TableSchema.fromBean(Course.class));
  }
}