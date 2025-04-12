package com.poc.camunda8.Adaptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Component
public class PatientMedicalRecordAdaptor {

    private final RabbitTemplate rabbitTemplate;
    private JdbcTemplate jdbcTemplate;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public PatientMedicalRecordAdaptor(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void init() {
        DataSource dataSource = new DriverManagerDataSource(
                "jdbc:postgresql://localhost:5432/hospital",
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
            case "age.request.queue" -> {
                column = "age";
                responseQueue = "age.response";
            }
            case "surgeries.request.queue" -> {
                column = "recent_surgery";
                responseQueue = "surgeries.response";
            }
            case "immunecompromization.request.queue" -> {
                column = "immune_compromised";
                responseQueue = "immunecompromization.response";
            }
            case "chronicillness.request.queue" -> {
                column = "chronic_illness";
                responseQueue = "chronicillness.response";
            }
            default -> {
                System.err.println("❌ Unknown queue: " + queue);
                return;
            }
        }

        try {
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            Integer patientId = Integer.valueOf(data.get("patientId").toString());

            System.out.println("📥 PatientMedicalRecordAdaptor: Fetching '" + column + "' for patientId = " + patientId);

            String sql = "SELECT " + column + " FROM patients WHERE id = ?";
            List<Object> results = jdbcTemplate.queryForList(sql, new Object[]{patientId}, Object.class);

            if (results.isEmpty()) {
                System.err.println("❌ No result found for patientId = " + patientId);
                return;
            }

            Object result = results.get(0);

            rabbitTemplate.convertAndSend("ExternalSystem.exchange", responseQueue, result);
            System.out.println("✅ Responded to " + responseQueue + " with: " + result);

              //print DB Contents
//            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM patients");
//
//            System.out.println("📋 Contents of 'patients' table:");
//            for (Map<String, Object> row : rows) {
//                System.out.println(row);
//            }

        } catch (Exception e) {
            System.err.println("❌ Failed query or message parsing: " + e.getMessage());
        }
    }
}
