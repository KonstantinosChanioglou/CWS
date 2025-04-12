package com.poc.camunda8.ExecutionEngine;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProcessExecutor {

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient zeebeClient;

    @PostConstruct
    public void init() {
        System.out.println("✅ SepsisTriggerController is initialized and active");
    }


    public void startProcessInstance(String processId, Map<String, Object> variables) {
        ProcessInstanceEvent event = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId(processId)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        System.out.println("✅ Ex. Engine: Process instance started with key: " + event.getProcessInstanceKey());
    }

    // This is triggered from external systems (from PMAdaptor)
    @RabbitListener(queues = "sepsis.execute.queue")
    public void executeProcess(Integer patientId) {
        System.out.println("✅ Ex. Engine: Message received to start sepsis workflow for patient: " + patientId);

        Map<String, Object> variables = new HashMap<>();
        variables.put("patientId", patientId);

        ProcessInstanceEvent event = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId("service-task-session")  // Your BPMN process ID (defined in your .bpmn file)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        System.out.println("✅ Ex. Engine: Process instance started with key: " + event.getProcessInstanceKey());
    }
}
