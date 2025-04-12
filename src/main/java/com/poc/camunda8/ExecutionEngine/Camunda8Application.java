package com.poc.camunda8.ExecutionEngine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.camunda.zeebe.spring.client.EnableZeebeClient;

@SpringBootApplication(scanBasePackages = "com.poc.camunda8")
@EnableZeebeClient
public class Camunda8Application {

	public static void main(String[] args) {
		SpringApplication.run(Camunda8Application.class, args);
	}
}
