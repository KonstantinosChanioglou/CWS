//package com.poc.camunda8;
//
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.http.ResponseEntity;
//
//@Component
//public class PMAdaptor {
//
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final RabbitTemplate rabbitTemplate;
//
//    public PMAdaptor(RabbitTemplate rabbitTemplate) {
//        this.rabbitTemplate = rabbitTemplate;
//    }
//
//    @RabbitListener(queues = "temperature.request.queue")
//    public void handleTemperatureRequest(String message) {
//
//        System.out.println("Received temperature request: " + message);
//
//        try {
//            // Set the external system URL (Your Flask server)
//            String externalSystemUrl = "http://localhost:8090/";
//
//            // Make a GET request to the Flask server
//            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);
//
//            // Print the response from the Flask server
//            System.out.println("Response from Flask server: " + response.getBody());
//
//            // Publish the temperature response to RabbitMQ
//            rabbitTemplate.convertAndSend("temperature.exchange", "temperature.response", response.getBody());
//
//        } catch (Exception e) {
//            System.err.println("Failed to reach external system: " + e.getMessage());
//        }
//    }
//}


package com.poc.camunda8.Adaptors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@RestController
@Component
public class PMAdaptor {

    private final RestTemplate restTemplate = new RestTemplate();
    private final RabbitTemplate rabbitTemplate;

    public PMAdaptor(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    //Handling temperature requests
    @RabbitListener(queues = "temperature.request.queue")
    public void handleTemperatureRequest(String message) {

        System.out.println("✅ PMAdaptor: Received temperature request: " + message);

        try {
            // Set the external system URL (Your Flask server)
            String externalSystemUrl = "http://localhost:8090/temperature";

            // Make a GET request to the Flask server
            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);

            // Print the response from the Flask server
            System.out.println("✅ PMAdaptor: Response from Patient Monitor: " + response.getBody());

            // Publish the temperature response to RabbitMQ
            rabbitTemplate.convertAndSend("ExternalSystem.exchange", "temperature.response", response.getBody());

        } catch (Exception e) {
            System.err.println("❌ PMAdaptor: Failed to reach Patient Monitor: " + e.getMessage());
        }
    }


    @RabbitListener(queues = "heartrate.request.queue")
    public void handleHeartRateRequest(String message) {

        System.out.println("✅ PMAdaptor: Received Heart Rate request: " + message);

        try {
            // Set the external system URL (Your Flask server)
            String externalSystemUrl = "http://localhost:8090/heartrate";

            // Make a GET request to the Flask server
            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);

            // Print the response from the Flask server
            System.out.println("✅ PMAdaptor: Response from Patient Monitor: " + response.getBody());

            // Publish the temperature response to RabbitMQ
            rabbitTemplate.convertAndSend("ExternalSystem.exchange", "heartrate.response", response.getBody());

        } catch (Exception e) {
            System.err.println("❌ PMAdaptor: Failed to reach Patient Monitor: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "respiration.request.queue")
    public void handleRespirationRateRequest(String message) {

        System.out.println("✅ PMAdaptor: Received Respiration Rate Pressure request: " + message);

        try {
            // Set the external system URL (Your Flask server)
            String externalSystemUrl = "http://localhost:8090/respirationrate";

            // Make a GET request to the Flask server
            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);

            // Print the response from the Flask server
            System.out.println("✅ PMAdaptor: Response from Patient Monitor: " + response.getBody());

            // Publish the temperature response to RabbitMQ
            rabbitTemplate.convertAndSend("ExternalSystem.exchange", "respiration.response", response.getBody());

        } catch (Exception e) {
            System.err.println("❌ PMAdaptor: Failed to reach Patient Monitor: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "systolicBloodPressure.request.queue")
    public void handleSystolicBloodPressureRequest(String message) {

        System.out.println("✅ PMAdaptor: Received Systolic Blood Pressure Pressure request: " + message);

        try {
            // Set the external system URL (Your Flask server)
            String externalSystemUrl = "http://localhost:8090/systolicBloodPressure";

            // Make a GET request to the Flask server
            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);

            // Print the response from the Flask server
            System.out.println("✅ PMAdaptor: Response from Patient Monitor: " + response.getBody());

            // Publish the temperature response to RabbitMQ
            rabbitTemplate.convertAndSend("ExternalSystem.exchange", "systolicBloodPressure.response", response.getBody());

        } catch (Exception e) {
            System.err.println("❌ PMAdaptor: Failed to reach Patient Monitor: " + e.getMessage());
        }
    }

    @RabbitListener(queues = "meanArterialPressure.request.queue")
    public void handleMeanArterialPressureRequest(String message) {

        System.out.println("✅ PMAdaptor: Received Mean Arterial Pressure request: " + message);

        try {
            // Set the external system URL (Your Flask server)
            String externalSystemUrl = "http://localhost:8090/meanArterialPressure";

            // Make a GET request to the Flask server
            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);

            // Print the response from the Flask server
            System.out.println("✅ PMAdaptor: Response from Patient Monitor: " + response.getBody());

            // Publish the temperature response to RabbitMQ
            rabbitTemplate.convertAndSend("ExternalSystem.exchange", "meanArterialPressure.response", response.getBody());

        } catch (Exception e) {
            System.err.println("❌ PMAdaptor: Failed to reach Patient Monitor: " + e.getMessage());
        }
    }


    //Trigger Sepsis Execution via HTTP Request
    @PostMapping("/trigger-sepsis")
    public String triggerSepsisExecution(@RequestBody(required = false) Map<String, String> body) {
        if (body == null || !body.containsKey("patientId")) {
            return "❌ PMAdaptor: Missing 'patientId' in request.";
        }

        String patientIdString = body.get("patientId").toString(); // handles numbers and strings
        int patientId = Integer.parseInt(patientIdString); // or use Integer if you need boxed


        rabbitTemplate.convertAndSend("sepsis.exchange", "sepsis.execute", patientId);

        System.out.println("✅ PMAdaptor: Published request to execute Sepsis workflow for patient: " + patientId);
        return "✅ PMAdaptor: Sepsis workflow request sent to RabbitMQ for patient: " + patientId;
    }

}
