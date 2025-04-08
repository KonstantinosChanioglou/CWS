package com.poc.camunda8;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProcessExecutor {

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient zeebeClient;

    public void startProcessInstance(String processId, Map<String, Object> variables) {
        ProcessInstanceEvent event = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId(processId)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        System.out.println("✅ Process instance started with key: " + event.getProcessInstanceKey());
    }

    // 🔥 Listener for Sepsis Workflow Execution Requests
    @RabbitListener(queues = "sepsis.execute.queue")
    public void executeProcess(String message) {
        System.out.println("✅ Message received to start process: " + message);

        Map<String, Object> variables = new HashMap<>();
        variables.put("request", message);

        ProcessInstanceEvent event = zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId("service-task-session")  // Your BPMN process ID (defined in your .bpmn file)
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        System.out.println("✅ Process instance started with key: " + event.getProcessInstanceKey());
    }
}
