package com.poc.camunda8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.camunda8.DynamicQueueManager;
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
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RespirationRate {

    private final RabbitTemplate rabbitTemplate;
    private final ZeebeClient zeebeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamicQueueManager dynamicQueueManager;

    public static final String RESPONSE_QUEUE = "respiration.response.queue";
    public static final String REQUEST_ROUTING_KEY = "respiration.request";
    public static final String RESPONSE_ROUTING_KEY = "respiration.response";
    private final Map<Long, Boolean> activeJobs = new ConcurrentHashMap<>();

    @Autowired
    public RespirationRate(RabbitTemplate rabbitTemplate,
                               @Qualifier("zeebeClientLifecycle") ZeebeClient zeebeClient,
                               DynamicQueueManager dynamicQueueManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.zeebeClient = zeebeClient;
        this.dynamicQueueManager = dynamicQueueManager;

        dynamicQueueManager.createQueueAndBinding(RESPONSE_QUEUE, RESPONSE_ROUTING_KEY);
    }

    @ZeebeWorker(type = "measureRespirationRate", autoComplete = false)
    public void measureRespirationRate(ActivatedJob job) {
        System.out.println("UF: Requesting Respiration rate from external system...");

        Map<String, Object> variables = job.getVariablesAsMap();
        Integer patientId = (Integer) variables.get("patientId");

        // Track this job as active
        activeJobs.put(job.getKey(), true);


        String requestMessage = "{\"request\":\"fetchRespirationRate\", \"jobKey\": " + job.getKey() + ", \"patientId\": " + patientId + "}";
        rabbitTemplate.convertAndSend(DynamicQueueManager.EXCHANGE_NAME, REQUEST_ROUTING_KEY, requestMessage);

        System.out.println("UF: Respiration rate request sent to message broker.");
    }

    @RabbitListener(queues = RESPONSE_QUEUE)
    public void receiveRespirationRateResponse(String message) throws JsonProcessingException {

        System.out.println("UF: Received Respiration Rate response from Adaptor: " + message);

        Map<String, Object> responseMap = objectMapper.readValue(message, Map.class);
        Integer rate = (Integer) responseMap.get("value");
        Long jobKey = (Long) responseMap.get("jobKey");

        System.out.println("UF: Extracted Respiration Rate Value: " + rate);
        System.out.println("UF: Extracted JobKey to mark complete: " + jobKey);

        // Check if jobKey is still tracked
        if (!activeJobs.containsKey(jobKey)) {
            System.err.println("No job key available to complete the process. Ignoring the response.");
            return;
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("respirationRate", rate);

            zeebeClient.newCompleteCommand(jobKey)
                    .variables(variables)
                    .send()
                    .join();

            System.out.println("UF: RespiraitonRate value sent to BPMN and marked as complete.");

            // Remove from active jobs to prevent duplicates
            activeJobs.remove(jobKey);

        } catch (Exception e) {
            System.err.println("Failed to complete the respiration rate job: " + e.getMessage());
        }
    }
}

