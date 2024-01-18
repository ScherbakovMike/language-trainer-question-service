package com.mikescherbakov.languagetrainerquestionservice.repository;

import com.mikescherbakov.languagetrainermodel.course.Question;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;

@Repository
@RequiredArgsConstructor
public class QuestionRepository {

  private final DynamoDbAsyncTable<Question> questionTable;
  private final DynamoDbAsyncIndex<Question> questionGSI;

  public Flux<Question> findAll() {
    return Flux.from(questionTable.scan().items());
  }

  public Flux<Question> findAllByCourseId(String courseId) {
    var query = QueryConditional.keyEqualTo(Key.builder()
        .partitionValue(courseId).build());
    return Flux.from(questionTable.query(query).flatMapIterable(Page::items));
  }

  public Mono<Question> findById(String id) {
    var query = QueryConditional.keyEqualTo(getGsiPartitionKey(id));
    return Mono.from(questionGSI.query(query).flatMapIterable(Page::items));
  }

  public Flux<Question> findByCourseIdAndId(String id, String courseId) {
    var query = QueryConditional.keyEqualTo(getTableKey(courseId, id));
    return Flux.from(questionTable.query(query).flatMapIterable(Page::items));
  }

  public Mono<Question> delete(String id, String courseId) {
    return Mono.fromCompletionStage(questionTable.deleteItem(getTableKey(courseId, id)));
  }

  public Mono<Integer> count() {
    ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
        .addAttributeToProject("id").build();
    AtomicInteger counter = new AtomicInteger(0);
    return Flux.from(questionTable.scan(scanEnhancedRequest))
        .doOnNext(page -> counter.getAndAdd(page.items().size()))
        .then(Mono.defer(() -> Mono.just(counter.get())));
  }

  public Mono<Question> update(Question entity) {
    var updateRequest = UpdateItemEnhancedRequest.builder(Question.class).item(entity).build();
    return Mono.fromCompletionStage(questionTable.updateItem(updateRequest));
  }

  public Mono<Question> save(Question entity) {
    var putRequest = PutItemEnhancedRequest.builder(Question.class).item(entity).build();
    return Mono.fromCompletionStage(questionTable.putItem(putRequest).thenApply(x -> entity));
  }

  private Key getTableKey(String courseId, String id) {
    return Key.builder()
        .partitionValue(courseId)
        .sortValue(id)
        .build();
  }

  private Key getGsiPartitionKey(String id) {
    return Key.builder()
        .partitionValue(id)
        .build();
  }
}
