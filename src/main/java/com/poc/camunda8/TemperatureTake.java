//package com.poc.camunda8;
//
//import org.springframework.stereotype.Component;
//import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
//
////Publish TakeTemperature topic to the broker.
//@Component
//public class TemperatureTake {
//    @ZeebeWorker(type="temperaturetaking", autoComplete = true)
//    public void temperaturetaking(){
//        System.out.println("temperaturetaking");
//    }
//}


package com.poc.camunda8;

//import org.springframework.stereotype.Component;
//import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
//import io.camunda.zeebe.client.api.command.CompleteJobCommandStep1;
//import io.camunda.zeebe.client.api.worker.JobClient;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//public class TemperatureTake {
//
//    @ZeebeWorker(type = "temperaturetaking", autoComplete = false)
//    public void temperaturetaking(JobClient client) {
//
//        System.out.println("Executing temperaturetaking...");
//
//        // Example temperature value returned by your worker
//        int temperature = 39;
//
//        // Create variables map to send back to Zeebe
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("temperature", temperature);
//
//        // Complete the job using client
//        CompleteJobCommandStep1 completeCommand = client.newCompleteCommand(0L) // Use jobKey = 0L or a valid value
//                .variables(variables);
//
//        completeCommand.send().join();
//
//        System.out.println("Temperature value sent back to BPMN: " + temperature);
//    }
//}


import org.springframework.stereotype.Component;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;


import java.util.HashMap;
import java.util.Map;

@Component
public class TemperatureTake {

    @ZeebeWorker(type = "temperaturetaking", autoComplete = false)
    public void temperaturetaking(JobClient client, ActivatedJob job) {

        System.out.println("Executing temperaturetaking...");

        int temperature = 36;

        Map<String, Object> variables = new HashMap<>();
        variables.put("temperature", temperature);

        // Use the actual job key
        long jobKey = job.getKey();

        client.newCompleteCommand(jobKey)  // This is the right way to complete the job
                .variables(variables)
                .send()
                .join();

        System.out.println("Temperature value sent back to BPMN: " + temperature);
    }
}
