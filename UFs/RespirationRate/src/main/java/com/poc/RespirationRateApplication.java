package com.poc;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
public class RespirationRateApplication {
    public static void main(String[] args) {
        SpringApplication.run(RespirationRateApplication.class, args);
    }
}
