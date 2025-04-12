package com.poc.camunda8.UFs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.camunda8.ExecutionEngine.DynamicQueueManager;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BloodPressure {

    private final RabbitTemplate rabbitTemplate;
    private final ZeebeClient zeebeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamicQueueManager dynamicQueueManager;

    public static final String REQUEST_QUEUE = "bloodpressure.request.queue";
    public static final String RESPONSE_QUEUE = "bloodpressure.response.queue";
    public static final String REQUEST_ROUTING_KEY = "bloodpressure.request";
    public static final String RESPONSE_ROUTING_KEY = "bloodpressure.response";

    private Long latestJobKey;

    @Autowired
    public BloodPressure(RabbitTemplate rabbitTemplate,
                         @Qualifier("zeebeClientLifecycle") ZeebeClient zeebeClient,
                         DynamicQueueManager dynamicQueueManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.zeebeClient = zeebeClient;
        this.dynamicQueueManager = dynamicQueueManager;

        // Ensure queues and bindings are created
        dynamicQueueManager.createQueueAndBinding(REQUEST_QUEUE, REQUEST_ROUTING_KEY);
        dynamicQueueManager.createQueueAndBinding(RESPONSE_QUEUE, RESPONSE_ROUTING_KEY);
    }

    @ZeebeWorker(type = "bloodPressureTaking", autoComplete = false)
    public void bloodPressureTaking(JobClient client, ActivatedJob job) throws JsonProcessingException {

        System.out.println("UF: Requesting blood pressure from external system...");

        this.latestJobKey = job.getKey();

        String requestMessage = "{\"request\":\"fetchBloodPressure\", \"jobKey\": " + job.getKey() + "}";
        rabbitTemplate.convertAndSend(DynamicQueueManager.EXCHANGE_NAME, REQUEST_ROUTING_KEY, requestMessage);

        System.out.println("UF: Blood pressure request sent to message broker.");
    }

    @RabbitListener(queues = RESPONSE_QUEUE)
    public void receiveBloodPressureResponse(String message) throws JsonProcessingException {

        System.out.println("UF: Received temperature response from Adaptor: " + message);

        Map<String, Object> responseMap = objectMapper.readValue(message, Map.class);
        Integer bp = (Integer) responseMap.get("value");

        System.out.println("UF: Extracted Blood Preassure Value: " + bp);

        if (latestJobKey == null) {
            System.err.println("No job key available to complete the process. Ignoring the response.");
            return;
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("bloodpreassure", bp);

            zeebeClient.newCompleteCommand(latestJobKey)
                    .variables(variables)
                    .send()
                    .join();

            System.out.println("UF: Blood pressure sent to BPMN and marked as complete.");
            latestJobKey = null;

        } catch (Exception e) {
            System.err.println("Failed to complete the job: " + e.getMessage());
        }
    }
}