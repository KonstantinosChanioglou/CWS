package com.poc;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
public class ImmuneCompromisationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ImmuneCompromisationApplication.class, args);
    }
}
