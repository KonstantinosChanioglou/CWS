package com.poc.camunda8;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import io.camunda.zeebe.client.ZeebeClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProcessExecutor {

    private ZeebeClient zeebeClient;
    private final DynamicQueueManager dynamicQueueManager;
    public static final String SEPSIS_EXECUTE_REQUEST_QUEUE = "sepsis.execute.queue";
    public static final String SEPSIS_EXECUTE_ROURING_KEY = "sepsis.execute";

    @Autowired
    public ProcessExecutor(ZeebeClient zeebeClient, DynamicQueueManager dynamicQueueManager) {
        this.zeebeClient = zeebeClient;
        this.dynamicQueueManager = dynamicQueueManager;
        dynamicQueueManager.createQueueAndBinding(SEPSIS_EXECUTE_REQUEST_QUEUE, SEPSIS_EXECUTE_ROURING_KEY);
        System.out.println("\uD83D\uDDF8 ProcessExecutor is initialized and active");
    }

    // This is triggered from external systems (from PMAdaptor)
    @RabbitListener(queues = "sepsis.execute.queue")
    public void executeProcess(Integer patientId) {
        System.out.println("Message received to start sepsis workflow for patient: " + patientId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("patientId", patientId);

        try {
            ProcessInstanceEvent event = zeebeClient
                    .newCreateInstanceCommand()
                    .bpmnProcessId("SepsisWorkflow")
                    .latestVersion()
                    .variables(variables)
                    .send()
                    .join();
            System.out.println("Process instance started with key: " + event.getProcessInstanceKey());

        } catch (Exception e) {
            System.err.println("Failed to start process: " + e.getMessage());
        }

    }
}
