package com.poc.camunda8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.camunda8.DynamicQueueManager;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final DynamicQueueManager dynamicQueueManager;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public static final String TEMPERATURE_REQUEST_QUEUE = "temperature.request.philips.pm.queue";
    public static final String HEART_RATE_REQUEST_QUEUE  = "heartrate.request.philips.pm.queue";
    public static final String RESPIRATION_RATE_REQUEST_QUEUE  = "respiration.request.philips.pm.queue";
    public static final String SYSTOLIC_BLOOD_PRESSURE_REQUEST_QUEUE  = "systolicBloodPressure.philips.pm.request.queue";
    public static final String MEAN_ARTERIAL_PRESSURE_REQUEST_QUEUE  = "meanArterialPressure.request.philips.pm.queue";

    public static final String TEMPERATURE_REQUEST_ROUTING_KEY = "temperature.request";
    public static final String HEART_RATE_REQUEST_ROUTING_KEY = "heartrate.request";
    public static final String RESPIRATION_RATE_REQUEST_ROUTING_KEY = "respiration.request";
    public static final String SYSTOLIC_BLOOD_PRESSURE_REQUEST_ROUTING_KEY = "systolicBloodPressure.request";
    public static final String MEAN_ARTERIAL_PRESSURE_REQUEST_ROUTING_KEY = "meanArterialPressure.request";

    public PMAdaptor(RabbitTemplate rabbitTemplate, DynamicQueueManager dynamicQueueManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.dynamicQueueManager = dynamicQueueManager;
        dynamicQueueManager.createQueueAndBinding(TEMPERATURE_REQUEST_QUEUE, TEMPERATURE_REQUEST_ROUTING_KEY);
        dynamicQueueManager.createQueueAndBinding(HEART_RATE_REQUEST_QUEUE, HEART_RATE_REQUEST_ROUTING_KEY);
        dynamicQueueManager.createQueueAndBinding(RESPIRATION_RATE_REQUEST_QUEUE, RESPIRATION_RATE_REQUEST_ROUTING_KEY);
        dynamicQueueManager.createQueueAndBinding(SYSTOLIC_BLOOD_PRESSURE_REQUEST_QUEUE, SYSTOLIC_BLOOD_PRESSURE_REQUEST_ROUTING_KEY);
        dynamicQueueManager.createQueueAndBinding(MEAN_ARTERIAL_PRESSURE_REQUEST_QUEUE, MEAN_ARTERIAL_PRESSURE_REQUEST_ROUTING_KEY);

    }

    //Handling temperature requests
    @RabbitListener(queues = TEMPERATURE_REQUEST_QUEUE)
    public void handleTemperatureRequest(String message) {

        System.out.println("PMAdaptorType1: Received temperature request: " + message);

        try {
            // Parse the incoming message to extract jobKey and patientId
            Map<String, Object> requestMap = objectMapper.readValue(message, Map.class);
            Long jobKey = Long.valueOf(requestMap.get("jobKey").toString());
            String patientId = requestMap.get("patientId").toString();

            // Set the external system URL. Here the correct PM based on the patient ID should be reached
            String externalSystemUrl = "http://host.docker.internal:8090/temperature";

            // Make a GET request to the Flask server
            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);

            String enrichedResponse = enrichResponse(response,jobKey,patientId);

            rabbitTemplate.convertAndSend("ExternalSystem.exchange", "temperature.response", enrichedResponse);

        } catch (Exception e) {
            System.err.println("PMAdaptorType1: Failed to handle request: " + e.getMessage());
        }
    }


    @RabbitListener(queues = HEART_RATE_REQUEST_QUEUE)
    public void handleHeartRateRequest(String message) {

        System.out.println("PMAdaptorType1: Received Heart Rate request: " + message);

        try {
            // Parse the incoming message to extract jobKey and patientId
            Map<String, Object> requestMap = objectMapper.readValue(message, Map.class);
            Long jobKey = Long.valueOf(requestMap.get("jobKey").toString());
            String patientId = requestMap.get("patientId").toString();

            // Set the external system URL. Here the correct PM based on the patient ID should be reached
            String externalSystemUrl = "http://host.docker.internal:8090/heartrate";

            // Make a GET request to the Flask server
            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);

            String enrichedResponse = enrichResponse(response, jobKey,patientId);

            rabbitTemplate.convertAndSend("ExternalSystem.exchange", "heartrate.response", enrichedResponse);

        } catch (Exception e) {
            System.err.println("PMAdaptorType1: Failed to handle request: " + e.getMessage());
        }
    }

    @RabbitListener(queues = RESPIRATION_RATE_REQUEST_QUEUE)
    public void handleRespirationRateRequest(String message) {

        System.out.println("PMAdaptorType1: Received Respiration Rate Pressure request: " + message);

        try {
            // Parse the incoming message to extract jobKey and patientId
            Map<String, Object> requestMap = objectMapper.readValue(message, Map.class);
            Long jobKey = Long.valueOf(requestMap.get("jobKey").toString());
            String patientId = requestMap.get("patientId").toString();

            // Set the external system URL. Here the correct PM based on the patient ID should be reached
            String externalSystemUrl = "http://host.docker.internal:8090/respirationrate";

            // Make a GET request to the Flask server
            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);

            String enrichedResponse = enrichResponse(response, jobKey,patientId);

            rabbitTemplate.convertAndSend("ExternalSystem.exchange", "respiration.response", enrichedResponse);

        } catch (Exception e) {
            System.err.println("PMAdaptorType1: Failed to handle request: " + e.getMessage());
        }
    }

    @RabbitListener(queues = SYSTOLIC_BLOOD_PRESSURE_REQUEST_QUEUE)
    public void handleSystolicBloodPressureRequest(String message) {

        System.out.println("PMAdaptorType1: Received Systolic Blood Pressure Pressure request: " + message);

        try {
            // Parse the incoming message to extract jobKey and patientId
            Map<String, Object> requestMap = objectMapper.readValue(message, Map.class);
            Long jobKey = Long.valueOf(requestMap.get("jobKey").toString());
            String patientId = requestMap.get("patientId").toString();

            // Set the external system URL. Here the correct PM based on the patient ID should be reached
            String externalSystemUrl = "http://host.docker.internal:8090/systolicBloodPressure";

            // Make a GET request to the Flask server
            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);

            String enrichedResponse = enrichResponse(response, jobKey,patientId);

            rabbitTemplate.convertAndSend("ExternalSystem.exchange", "systolicBloodPressure.response", enrichedResponse);

        } catch (Exception e) {
            System.err.println("PMAdaptorType1: Failed to handle request: " + e.getMessage());
        }
    }

    @RabbitListener(queues = MEAN_ARTERIAL_PRESSURE_REQUEST_QUEUE)
    public void handleMeanArterialPressureRequest(String message) {

        System.out.println("PMAdaptorType1: Received Mean Arterial Pressure request: " + message);

        try {
            // Parse the incoming message to extract jobKey and patientId
            Map<String, Object> requestMap = objectMapper.readValue(message, Map.class);
            Long jobKey = Long.valueOf(requestMap.get("jobKey").toString());
            String patientId = requestMap.get("patientId").toString();

            // Set the external system URL. Here the correct PM based on the patient ID should be reached
            String externalSystemUrl = "http://host.docker.internal:8090/meanArterialPressure";

            // Make a GET request to the Flask server
            ResponseEntity<String> response = restTemplate.getForEntity(externalSystemUrl, String.class);

            String enrichedResponse = enrichResponse(response, jobKey,patientId);

            rabbitTemplate.convertAndSend("ExternalSystem.exchange", "meanArterialPressure.response", enrichedResponse);

        } catch (Exception e) {
            System.err.println("PMAdaptorType1: Failed to handle request: " + e.getMessage());
        }
    }


    //Trigger Sepsis Execution via HTTP Request
    @PostMapping("/trigger-sepsis")
    public String triggerSepsisExecution(@RequestBody(required = false) Map<String, String> body) {

        if (body == null || !body.containsKey("patientId")) {
            return "PMAdaptorType1: Missing 'patientId' in request.";
        }

        String patientIdString = body.get("patientId").toString(); // handles numbers and strings
        int patientId = Integer.parseInt(patientIdString); // or use Integer if you need boxed


        rabbitTemplate.convertAndSend("ExternalSystem.exchange", "sepsis.execute", patientId);

        System.out.println("PMAdaptorType1: Published request to execute Sepsis workflow for patient: " + patientId);
        return "PMAdaptorType1: Sepsis workflow request sent to RabbitMQ for patient: " + patientId;
    }

    //Helper Function
    public String enrichResponse(ResponseEntity<String> response, Long jobKey, String patientId) throws JsonProcessingException {

        // Add jobKey and patientId to the outgoing message
        Map<String, Object> responseBody = objectMapper.readValue(response.getBody(), Map.class);
        responseBody.put("jobKey", jobKey);
        responseBody.put("patientId", patientId);
        responseBody.put("adaptor", "PM Adaptor Type 1");

        // Convert to JSON and return
        return objectMapper.writeValueAsString(responseBody);
    }

}
