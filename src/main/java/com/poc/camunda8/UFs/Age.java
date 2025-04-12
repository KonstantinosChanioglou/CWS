package com.poc.camunda8.UFs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.camunda8.ExecutionEngine.DynamicQueueManager;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Age {

    private final RabbitTemplate rabbitTemplate;
    private final ZeebeClient zeebeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamicQueueManager dynamicQueueManager;

    public static final String REQUEST_QUEUE = "age.request.queue";
    public static final String RESPONSE_QUEUE = "age.response.queue";
    public static final String REQUEST_ROUTING_KEY = "age.request";
    public static final String RESPONSE_ROUTING_KEY = "age.response";

    private Long latestJobKey;

    @Autowired
    public Age(RabbitTemplate rabbitTemplate,
               @Qualifier("zeebeClientLifecycle") ZeebeClient zeebeClient,
               DynamicQueueManager dynamicQueueManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.zeebeClient = zeebeClient;
        this.dynamicQueueManager = dynamicQueueManager;

        dynamicQueueManager.createQueueAndBinding(REQUEST_QUEUE, REQUEST_ROUTING_KEY);
        dynamicQueueManager.createQueueAndBinding(RESPONSE_QUEUE, RESPONSE_ROUTING_KEY);

    }

    @ZeebeWorker(type = "checkAge", autoComplete = false)
    public void checkAge(JobClient client, ActivatedJob job) {
        System.out.println("UF: Requesting Age from external system...");

        Map<String, Object> variables = job.getVariablesAsMap();
        Integer patientId = (Integer) variables.get("patientId"); // ✅ extract patientId

        String requestMessage = "{\"patientId\":\"" + patientId + "\", \"jobKey\": " + job.getKey() + "}";

        rabbitTemplate.convertAndSend(DynamicQueueManager.EXCHANGE_NAME, REQUEST_ROUTING_KEY, requestMessage);
        System.out.println("UF: Age request sent to message broker for patientId: " + patientId);

        this.latestJobKey = job.getKey(); // keep this if you're handling correlation later
    }


    @RabbitListener(queues = RESPONSE_QUEUE)
    public void receiveAgeResponse(String message) throws JsonProcessingException {
        System.out.println("UF: Back to age UF received message: " + message);

        Integer age = Integer.valueOf(message); // directly convert

        if (latestJobKey == null) return;

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("age", age);

            zeebeClient.newCompleteCommand(latestJobKey)
                    .variables(variables)
                    .send()
                    .join();

            latestJobKey = null;
        } catch (Exception e) {
            System.err.println("Failed to complete the age job: " + e.getMessage());
        }
    }
}
