package com.mikescherbakov.languagetrainerquestionservice.repository;

import com.mikescherbakov.languagetrainermodel.course.Course;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.UpdateItemEnhancedRequest;

@Repository
@RequiredArgsConstructor
public class CourseRepository {

  private final DynamoDbAsyncTable<Course> courseTable;

  public Flux<Course> findAll() {
    return Flux.from(courseTable.scan().items());
  }

  public Mono<Course> findById(String id) {
    return Mono.fromFuture(courseTable.getItem(getKeyBuild(id)));
  }

  public Mono<Course> delete(String id) {
    return Mono.fromCompletionStage(courseTable.deleteItem(getKeyBuild(id)));
  }

  public Mono<Integer> count() {
    ScanEnhancedRequest scanEnhancedRequest = ScanEnhancedRequest.builder()
        .addAttributeToProject("id").build();
    AtomicInteger counter = new AtomicInteger(0);
    return Flux.from(courseTable.scan(scanEnhancedRequest))
        .doOnNext(page -> counter.getAndAdd(page.items().size()))
        .then(Mono.defer(() -> Mono.just(counter.get())));
  }

  public Mono<Course> update(Course entity) {
    var updateRequest = UpdateItemEnhancedRequest.builder(Course.class).item(entity).build();
    return Mono.fromCompletionStage(courseTable.updateItem(updateRequest));
  }

  public Mono<Course> save(Course entity) {
    var putRequest = PutItemEnhancedRequest.builder(Course.class).item(entity).build();
    return Mono.fromCompletionStage(courseTable.putItem(putRequest).thenApply(x -> entity));
  }

  private Key getKeyBuild(String id) {
    return Key.builder().partitionValue(id).build();
  }
}
