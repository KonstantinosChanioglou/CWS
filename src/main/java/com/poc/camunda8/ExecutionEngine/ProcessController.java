package com.poc.camunda8.ExecutionEngine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/process")
public class ProcessController {

    @Autowired
    private ProcessDeployer processDeployer;

    @Autowired
    private ProcessExecutor processExecutor;

    @PostMapping("/deploy")
    public String deploy(@RequestParam String filePath) {
        processDeployer.deployProcess(filePath);
        return "Deployment triggered for file: " + filePath;
    }

    //This is triggering for manual trigger with HTTP
    @PostMapping("/execute")
    public String execute(@RequestParam String processId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("temperature", 39);  // Example variable

        processExecutor.startProcessInstance(processId, variables);
        return "Execution triggered for process: " + processId;
    }
}
