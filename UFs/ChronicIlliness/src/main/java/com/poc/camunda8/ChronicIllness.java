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

@Component
public class ChronicIllness {

    public static final String RESPONSE_QUEUE = "chronicillness.response.queue";
    public static final String REQUEST_ROUTING_KEY = "chronicillness.request";
    public static final String RESPONSE_ROUTING_KEY = "chronicillness.response";

    private final RabbitTemplate rabbitTemplate;
    private final ZeebeClient zeebeClient;
    private final DynamicQueueManager dynamicQueueManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ChronicIllness(RabbitTemplate rabbitTemplate,
                          @Qualifier("zeebeClientLifecycle") ZeebeClient zeebeClient,
                          DynamicQueueManager dynamicQueueManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.zeebeClient = zeebeClient;
        this.dynamicQueueManager = dynamicQueueManager;

        dynamicQueueManager.createQueueAndBinding(RESPONSE_QUEUE, RESPONSE_ROUTING_KEY);
    }

    @ZeebeWorker(type = "checkChronicIllness", autoComplete = false)
    public void checkChronicIllness(ActivatedJob job) {
        System.out.println("UF: Requesting Chronic Illness from external system...");

        Map<String, Object> variables = job.getVariablesAsMap();
        Integer patientId = (Integer) variables.get("patientId");

        String requestMessage = "{\"patientId\":\"" + patientId + "\", \"jobKey\": " + job.getKey() + "}";

        rabbitTemplate.convertAndSend(DynamicQueueManager.EXCHANGE_NAME, "chronicillness.request", requestMessage);
        System.out.println("UF: Chronic Illness request sent for patientId: " + patientId);
    }

    @RabbitListener(queues = "chronicillness.response.queue")
    public void receiveChronicIllnessResponse(String message) throws JsonProcessingException {
        System.out.println("UF: Received Chronic Illness response: " + message);

        Map<String, Object> responseMap = objectMapper.readValue(message, Map.class);
        Boolean hasChronicIllness = (Boolean) responseMap.get("value");
        Long jobKey = (Long) responseMap.get("jobKey");

        System.out.println("UF: Extracted Respiration Rate Value: " + hasChronicIllness);
        System.out.println("UF: Extracted JobKey to mark complete: " + jobKey);

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("chronicIllness", hasChronicIllness);

            zeebeClient.newCompleteCommand(jobKey)
                    .variables(variables)
                    .send()
                    .join();

        } catch (Exception e) {
            System.err.println("Failed to complete chronic illness job: " + e.getMessage());
        }
    }

}
