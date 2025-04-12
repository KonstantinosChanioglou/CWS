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
public class HeartRate {

    private final RabbitTemplate rabbitTemplate;
    private final ZeebeClient zeebeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamicQueueManager dynamicQueueManager;

    public static final String REQUEST_QUEUE = "heartrate.request.queue";
    public static final String RESPONSE_QUEUE = "heartrate.response.queue";
    public static final String REQUEST_ROUTING_KEY = "heartrate.request";
    public static final String RESPONSE_ROUTING_KEY = "heartrate.response";

    private Long latestJobKey;

    @Autowired
    public HeartRate(RabbitTemplate rabbitTemplate,
                         @Qualifier("zeebeClientLifecycle") ZeebeClient zeebeClient,
                         DynamicQueueManager dynamicQueueManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.zeebeClient = zeebeClient;
        this.dynamicQueueManager = dynamicQueueManager;

        dynamicQueueManager.createQueueAndBinding(REQUEST_QUEUE, REQUEST_ROUTING_KEY);
        dynamicQueueManager.createQueueAndBinding(RESPONSE_QUEUE, RESPONSE_ROUTING_KEY);
    }

    @ZeebeWorker(type = "checkHeartRate", autoComplete = false)
    public void checkHeartRate(JobClient client, ActivatedJob job) {
        this.latestJobKey = job.getKey();
        String requestMessage = "{\"request\":\"fetchHeartRate\", \"jobKey\": " + job.getKey() + "}";
        rabbitTemplate.convertAndSend(DynamicQueueManager.EXCHANGE_NAME, REQUEST_ROUTING_KEY, requestMessage);
    }

    @RabbitListener(queues = RESPONSE_QUEUE)
    public void receiveHeartRateResponse(String message) throws JsonProcessingException {
        Map<String, Object> responseMap = objectMapper.readValue(message, Map.class);
        Integer heartRate = (Integer) responseMap.get("value");

        if (latestJobKey == null) return;

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("heartRate", heartRate);

            zeebeClient.newCompleteCommand(latestJobKey)
                    .variables(variables)
                    .send()
                    .join();

            latestJobKey = null;
        } catch (Exception e) {
            System.err.println("Failed to complete the heart rate job: " + e.getMessage());
        }
    }
}
