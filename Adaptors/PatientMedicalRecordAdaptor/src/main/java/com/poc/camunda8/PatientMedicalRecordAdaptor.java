package com.poc.camunda8;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;


@Component
public class PatientMedicalRecordAdaptor {

    private final RabbitTemplate rabbitTemplate;
    private JdbcTemplate jdbcTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final DynamicQueueManager dynamicQueueManager;

    public static final String AGE_REQUEST_QUEUE = "age.request.queue";
    public static final String SURGERIES_REQUEST_QUEUE  = "surgeries.request.queue";
    public static final String IMMUNECOMPROMIZATION_REQUEST_QUEUE  = "immunecompromization.request.queue";
    public static final String CHRONICILLNESS_REQUEST_QUEUE = "chronicillness.request.queue";

    public static final String AGE_REQUEST_ROUTING_KEY = "age.request";
    public static final String SURGERIES_RATE_REQUEST_ROUTING_KEY = "surgeries.request";
    public static final String IMMUNECOMPROMIZATION_REQUEST_ROUTING_KEY = "immunecompromization.request";
    public static final String CHRONICILLNESS_REQUEST_ROUTING_KEY = "chronicillness.request";


    public PatientMedicalRecordAdaptor(RabbitTemplate rabbitTemplate, DynamicQueueManager dynamicQueueManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.dynamicQueueManager = dynamicQueueManager;
        dynamicQueueManager.createQueueAndBinding(AGE_REQUEST_QUEUE, AGE_REQUEST_ROUTING_KEY);
        dynamicQueueManager.createQueueAndBinding(SURGERIES_REQUEST_QUEUE, SURGERIES_RATE_REQUEST_ROUTING_KEY);
        dynamicQueueManager.createQueueAndBinding(IMMUNECOMPROMIZATION_REQUEST_QUEUE, IMMUNECOMPROMIZATION_REQUEST_ROUTING_KEY);
        dynamicQueueManager.createQueueAndBinding(CHRONICILLNESS_REQUEST_QUEUE, CHRONICILLNESS_REQUEST_ROUTING_KEY);

    }

    @PostConstruct
    public void init() {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:postgresql://host.docker.internal:5432/hospital",
                "user",
                "password"
        );
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @RabbitListener(queues = {
            "age.request.queue",
            "surgeries.request.queue",
            "immunecompromization.request.queue",
            "chronicillness.request.queue"
    })
    public void handleMedicalQuery(@Payload String payload, Message rawMsg) {
        String queue = rawMsg.getMessageProperties().getConsumerQueue();
        String column;
        String responseQueue;

        switch (queue) {
            case "age.request.queue":
                column = "age";
                responseQueue = "age.response";
                break;
            case "surgeries.request.queue":
                column = "recent_surgery";
                responseQueue = "surgeries.response";
                break;
            case "immunecompromization.request.queue":
                column = "immune_compromised";
                responseQueue = "immunecompromization.response";
                break;
            case "chronicillness.request.queue":
                column = "chronic_illness";
                responseQueue = "chronicillness.response";
                break;
            default:
                System.err.println("❌ Unknown queue: " + queue);
                return;
        }

        try {
            Map<String, Object> requestMap = objectMapper.readValue(payload, Map.class);
            Integer patientId = Integer.valueOf(requestMap.get("patientId").toString());
            Long jobKey = Long.valueOf(requestMap.get("jobKey").toString());

            System.out.println("📥 PatientMedicalRecordAdaptor: Fetching '" + column + "' for patientId = " + patientId);

            String sql = "SELECT " + column + " FROM patients WHERE patientId = ?";
            List<Object> results = jdbcTemplate.queryForList(sql, new Object[]{patientId}, Object.class);

            if (results.isEmpty()) {
                System.err.println("❌ No result found for patientId = " + patientId);
                return;
            }

            Object result = results.get(0);
            String enrichedResponse = enrichResponse(result, jobKey, patientId.toString());

            // Send to RabbitMQ
            rabbitTemplate.convertAndSend("ExternalSystem.exchange", responseQueue, enrichedResponse);
            System.out.println("✅ Responded to " + responseQueue + " with: " + enrichedResponse);

        } catch (Exception e) {
            System.err.println("❌ Failed query or message parsing: " + e.getMessage());
        }
    }

    //Helper Function
    public String enrichResponse(Object result, Long jobKey, String patientId) throws JsonProcessingException {

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("value", result); // since result is just the value
        responseBody.put("jobKey", jobKey);
        responseBody.put("patientId", patientId);

        return objectMapper.writeValueAsString(responseBody);
    }

}

