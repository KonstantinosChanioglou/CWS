package com.poc;

import com.poc.camunda8.ProcessExecutor;
import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableZeebeClient
@SpringBootApplication
public class WorkflowRequestControllerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WorkflowRequestControllerApplication.class, args);
    }
}
