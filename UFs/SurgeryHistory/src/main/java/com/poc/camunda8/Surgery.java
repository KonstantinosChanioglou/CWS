package com.poc.camunda8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.camunda8.DynamicQueueManager;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Surgery {

    public static final String RESPONSE_QUEUE = "surgeries.response.queue";
    public static final String REQUEST_ROUTING_KEY = "surgeries.request";
    public static final String RESPONSE_ROUTING_KEY = "surgeries.response";

    private final RabbitTemplate rabbitTemplate;
    private final ZeebeClient zeebeClient;
    private final DynamicQueueManager dynamicQueueManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, Boolean> activeJobs = new ConcurrentHashMap<>();

    @Autowired
    public Surgery(RabbitTemplate rabbitTemplate,
                         @Qualifier("zeebeClientLifecycle") ZeebeClient zeebeClient,
                         DynamicQueueManager dynamicQueueManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.zeebeClient = zeebeClient;
        this.dynamicQueueManager = dynamicQueueManager;

        dynamicQueueManager.createQueueAndBinding(RESPONSE_QUEUE, RESPONSE_ROUTING_KEY);
    }

    @ZeebeWorker(type = "checkRecentSurgery", autoComplete = false)
    public void checkRecentSurgery(JobClient client, ActivatedJob job) {
        System.out.println("UF: Requesting Surgery status from external system...");

        Map<String, Object> variables = job.getVariablesAsMap();
        Integer patientId = (Integer) variables.get("patientId");

        // Track this job as active
        activeJobs.put(job.getKey(), true);

        String requestMessage = "{\"patientId\":\"" + patientId + "\", \"jobKey\": " + job.getKey() + "}";

        rabbitTemplate.convertAndSend(DynamicQueueManager.EXCHANGE_NAME, "surgeries.request", requestMessage);
        System.out.println("UF: Surgery request sent for patientId: " + patientId);

    }

    @RabbitListener(queues = "surgeries.response.queue")
    public void receiveRecentSurgeryResponse(String message) throws JsonProcessingException{
        System.out.println("UF: Received Surgery response: " + message);

        Map<String, Object> responseMap = objectMapper.readValue(message, Map.class);
        Boolean hadSurgery = (Boolean) responseMap.get("value");
        Long jobKey = (Long) responseMap.get("jobKey");

        System.out.println("UF: Extracted Surgery Value: " + hadSurgery);
        System.out.println("UF: Extracted JobKey to mark complete: " + jobKey);

        // Check if jobKey is still tracked
        if (!activeJobs.containsKey(jobKey)) {
            System.err.println("No job key available to complete the process. Ignoring the response.");
            return;
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("recentSurgery", hadSurgery);

            zeebeClient.newCompleteCommand(jobKey)
                    .variables(variables)
                    .send()
                    .join();

            System.out.println("UF: Surgery value sent to BPMN and marked as complete.");

            // Remove from active jobs to prevent duplicates
            activeJobs.remove(jobKey);

        } catch (Exception e) {
            System.err.println("Failed to complete surgery job: " + e.getMessage());
        }
    }

}
