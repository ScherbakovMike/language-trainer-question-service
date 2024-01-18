package com.mikescherbakov.languagetrainerquestionservice.controller;

import com.mikescherbakov.languagetrainermodel.course.Course;
import com.mikescherbakov.languagetrainermodel.dto.ApiResponse;
import com.mikescherbakov.languagetrainerquestionservice.repository.CourseRepository;
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
@RequestMapping("/courses")
public class CourseController {

  private final CourseRepository repository;

  @Autowired
  public CourseController(CourseRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public Mono<ApiResponse> getAllCourses() {
    return repository.findAll()
        .collectList()
        .map(courses -> new ApiResponse(courses,
            MessageFormat.format("{0} result found", courses.size())));
  }

  @GetMapping("/count")
  public Mono<ApiResponse> courseCount() {
    return repository.count()
        .map(count -> new ApiResponse(count, MessageFormat.format("Count of Courses: {0}", count)));
  }

  @GetMapping("/{id}")
  public Mono<ApiResponse> getByCourseId(@PathVariable String id) {
    return repository.findById(id)
        .map(course -> new ApiResponse(course, MessageFormat.format("Result found: {0}", course)))
        .defaultIfEmpty(new ApiResponse(null, "Course not found"));
  }

  @PostMapping
  public Mono<ResponseEntity<ApiResponse>> create(@RequestBody Mono<Course> course) {
    return course
        .flatMap(repository::save)
        .map(course1 -> ResponseEntity.status(HttpStatus.CREATED)
            .body(new ApiResponse(course1, "Course successfully created")));
  }

  @PutMapping("/{id}")
  public Mono<ApiResponse> update(@PathVariable String id, @RequestBody Mono<Course> course) {
    return course
        .map(course1 -> {
          course1.setId(id);
          return course1;
        })
        .flatMap(repository::update)
        .map(courseUpdated -> new ApiResponse(courseUpdated, "Course successfully updated"));
  }

  @DeleteMapping("/{id}")
  public Mono<ApiResponse> delete(@PathVariable String id) {
    return repository.delete(id)
        .map(courseDeleted -> new ApiResponse(courseDeleted, "Course successfully deleted"));
  }
}
