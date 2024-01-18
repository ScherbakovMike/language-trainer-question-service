package com.mikescherbakov.languagetrainerquestionservice.aws;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.mikescherbakov.languagetrainerquestionservice.LanguageTrainerQuestionServiceApplication;
import java.io.InputStream;
import java.io.OutputStream;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class LambdaStreamHandler implements RequestStreamHandler {

  private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

  static {
    try {
      handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(
          LanguageTrainerQuestionServiceApplication.class);
    } catch (ContainerInitializationException e) {
      // if we fail here. We re-throw the exception to force another cold start
      log.error(e.getMessage(), e);
      throw new RuntimeException("Could not initialize Spring Boot application", e);
    }
  }

  @Override
  public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
    try {
      MDC.put("awsRequestId", context.getAwsRequestId());
      handler.proxyStream(inputStream, outputStream, context);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException("Lambda handle exception", e);
    }
  }

}
