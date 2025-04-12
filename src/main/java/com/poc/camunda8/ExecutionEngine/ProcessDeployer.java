package com.poc.camunda8.ExecutionEngine;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.Process;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class ProcessDeployer {

    @Qualifier("zeebeClientLifecycle")
    @Autowired
    private ZeebeClient zeebeClient;

    public void deployProcess(String bpmnFilePath) {
        try {
            byte[] fileContent = Files.readAllBytes(Path.of(bpmnFilePath));

            DeploymentEvent response = zeebeClient
                    .newDeployCommand()
                    .addResourceBytes(fileContent, bpmnFilePath)
                    .send()
                    .join();

            List<Process> deployedProcesses = response.getProcesses();

            for (Process process : deployedProcesses) {
                System.out.println("✅ Ex. Enigne: Deployed process: " + process.getBpmnProcessId()
                        + " | Version: " + process.getVersion()
                        + " | Process Definition Key: " + process.getProcessDefinitionKey());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
