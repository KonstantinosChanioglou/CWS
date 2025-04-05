package com.poc.camunda8;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = "classpath:sample.bpmn")
public class Camunda8Application {

	public static void main(String[] args) {
		SpringApplication.run(Camunda8Application.class, args);
	}

}
