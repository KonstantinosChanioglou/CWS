package com.poc;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableZeebeClient
public class TemperatureApplication {
    public static void main(String[] args) {
        SpringApplication.run(TemperatureApplication.class, args);
    }
}
