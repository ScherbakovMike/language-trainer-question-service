package com.mikescherbakov.languagetrainerquestionservice.controller;

import com.mikescherbakov.languagetrainermodel.course.Question;
import com.mikescherbakov.languagetrainermodel.dto.ApiResponse;
import com.mikescherbakov.languagetrainerquestionservice.repository.QuestionRepository;
import java.text.MessageFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/questions")
public class QuestionController {

  public static final String QUESTIONS_NOT_FOUND = "Questions not found";

  private final QuestionRepository repository;

  @Autowired
  public QuestionController(QuestionRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public Mono<ApiResponse> getAllQuestions() {
    return repository.findAll()
        .collectList()
        .map(questions -> new ApiResponse(questions,
            MessageFormat.format("{0} result found", questions.size())));
  }

  @GetMapping("/count")
  public Mono<ApiResponse> questionCount() {
    return repository.count()
        .map(count -> new ApiResponse(count,
            MessageFormat.format("Count of Questions: {0}", count)));
  }

  @GetMapping("/{id}")
  public Mono<ApiResponse> getById(@PathVariable String id) {
    return repository.findById(id)
        .map(question -> new ApiResponse(question, MessageFormat.format("Result found: {0}", question)))
        .defaultIfEmpty(new ApiResponse(null, QUESTIONS_NOT_FOUND));
  }

  @GetMapping("/{courseId}/{id}")
  public Mono<ApiResponse> getByCourseIdQuestionId(@PathVariable String courseId, @PathVariable String id) {
    return repository.findByCourseIdAndId(id, courseId)
        .collectList()
        .map(list -> new ApiResponse(list,
            MessageFormat.format("Result found: {0} elements", list.size())))
        .defaultIfEmpty(new ApiResponse(null, QUESTIONS_NOT_FOUND));
  }

  @GetMapping("/{courseId}/")
  public Mono<ApiResponse> getAllQuestionsByCourseId(@PathVariable String courseId) {
    return repository.findAllByCourseId(courseId)
        .collectList()
        .map(list -> new ApiResponse(list,
            MessageFormat.format("Result found: {0} elements", list.size())))
        .defaultIfEmpty(new ApiResponse(null, QUESTIONS_NOT_FOUND));
  }

  @PostMapping
  public Mono<ResponseEntity<ApiResponse>> create(@RequestBody Mono<Question> question) {
    return question
        .flatMap(repository::save)
        .map(question1 -> ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse(question1, "Question successfully created")));
  }

  @PutMapping("/{id}")
  public Mono<ApiResponse> update(@PathVariable String id, @RequestBody Mono<Question> question) {
    return question
        .map(question1 -> {
          question1.setId(id);
          return question1;
        })
        .flatMap(repository::update)
        .map(questionUpdated -> new ApiResponse(questionUpdated, "Question successfully updated"));
  }

  @DeleteMapping("/{courseId}/{id}")
  public Mono<ApiResponse> delete(@PathVariable String courseId, @PathVariable String id) {
    return repository.delete(id, courseId)
        .map(questionDeleted -> new ApiResponse(questionDeleted, "Question successfully deleted"));
  }
}
