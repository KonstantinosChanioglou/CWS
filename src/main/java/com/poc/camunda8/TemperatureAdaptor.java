package com.poc.camunda8;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;

@Component
public class TemperatureAdaptor {

    private final RestTemplate restTemplate = new RestTemplate();
    private final RabbitTemplate rabbitTemplate;

    public TemperatureAdaptor(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "temperature.request.queue")
    public void handleTemperatureRequest(String message) {

        System.out.println("Received temperature request: " + message);

        try {
            // Set the external system URL (Your Flask server)
            String externalSystemUrl = "http://localhost:8090/";

            // Make a GET request to the Flask server
            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);

            // Print the response from the Flask server
            System.out.println("Response from Flask server: " + response.getBody());

            // Publish the temperature response to RabbitMQ
            rabbitTemplate.convertAndSend("temperature.exchange", "temperature.response", response.getBody());

        } catch (Exception e) {
            System.err.println("Failed to reach external system: " + e.getMessage());
        }
    }
}
