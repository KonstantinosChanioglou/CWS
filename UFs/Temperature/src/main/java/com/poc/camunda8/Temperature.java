package com.poc.camunda8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Temperature {

    private final RabbitTemplate rabbitTemplate;
    private final ZeebeClient zeebeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamicQueueManager dynamicQueueManager;

    public static final String RESPONSE_QUEUE = "temperature.response.queue";
    public static final String REQUEST_ROUTING_KEY = "temperature.request";
    public static final String RESPONSE_ROUTING_KEY = "temperature.response";
    private final Map<Long, Boolean> activeJobs = new ConcurrentHashMap<>();


    @Autowired
    public Temperature(RabbitTemplate rabbitTemplate, ZeebeClient zeebeClient, DynamicQueueManager dynamicQueueManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.zeebeClient = zeebeClient;
        this.dynamicQueueManager = dynamicQueueManager;

        // Ensure queues and bindings are created
        dynamicQueueManager.createQueueAndBinding(RESPONSE_QUEUE, RESPONSE_ROUTING_KEY);
    }

    @ZeebeWorker(type = "measureTemperature", autoComplete = false)
    public void measureTemperature(ActivatedJob job) throws JsonProcessingException {

        System.out.println("UF: Requesting temperature from external system...");

        Map<String, Object> variables = job.getVariablesAsMap();
        Integer patientId = (Integer) variables.get("patientId");

        // Track this job as active
        activeJobs.put(job.getKey(), true);

        String requestMessage = "{\"request\":\"fetchTemperature\", \"jobKey\": " + job.getKey() + ", \"patientId\": " + patientId + "}";
        rabbitTemplate.convertAndSend(DynamicQueueManager.EXCHANGE_NAME, REQUEST_ROUTING_KEY, requestMessage);

        System.out.println("UF: Temperature request sent to message broker.");
    }

    @RabbitListener(queues = RESPONSE_QUEUE)
    public void receiveTemperatureResponse(String message) throws JsonProcessingException {

        System.out.println("UF: Received temperature response from Adaptor: " + message);

        Map<String, Object> responseMap = objectMapper.readValue(message, Map.class);
        Integer temperature = (Integer) responseMap.get("value");
        Long jobKey = (Long) responseMap.get("jobKey");

        // Simulate failure for demonstration
        if (false) { // Replace `true` with any condition if you want dynamic failure
            zeebeClient.newFailCommand(jobKey)
                    .retries(0) // Set to 0 to trigger incident and show retry button
                    .errorMessage("Simulated failure in Temperature UF")
                    .send()
                    .join();

            System.err.println("UF: Simulated failure sent to Camunda.");
            return;
        }

        // Check if jobKey is still tracked
        if (!activeJobs.containsKey(jobKey)) {
            System.err.println("Ignoring the response. Already a successful temperature measurement response has benn recieved");

            //System.err.println("No job key available to complete the process. Ignoring the response.");
            return;
        }else{
            System.out.println("UF: Extracted Temperature Value: " + temperature);
            System.out.println("UF: Extracted JobKey to mark complete: " + jobKey);
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("temperature", temperature);

            zeebeClient.newCompleteCommand(jobKey)
                    .variables(variables)
                    .send()
                    .join();

            System.out.println("UF: Temperature value sent to BPMN and marked as complete.");

            // Remove from active jobs to prevent duplicates
            activeJobs.remove(jobKey);

        } catch (Exception e) {
            System.err.println("Failed to complete the job: " + e.getMessage());
        }
    }
}


