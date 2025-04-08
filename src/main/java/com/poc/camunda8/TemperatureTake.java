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



//import org.springframework.stereotype.Component;
//import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
//import io.camunda.zeebe.client.api.worker.JobClient;
//import io.camunda.zeebe.client.api.response.ActivatedJob;
//
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//public class TemperatureTake {
//
//    @ZeebeWorker(type = "temperaturetaking", autoComplete = false)
//    public void temperaturetaking(JobClient client, ActivatedJob job) {
//
//        System.out.println("Executing temperaturetaking...");
//
//        int temperature = 36;
//
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("temperature", temperature);
//
//        // Use the actual job key
//        long jobKey = job.getKey();
//
//        client.newCompleteCommand(jobKey)  // This is the right way to complete the job
//                .variables(variables)
//                .send()
//                .join();
//
//        System.out.println("Temperature value sent back to BPMN: " + temperature);
//    }
//}


//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.stereotype.Component;
//import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
//import io.camunda.zeebe.client.api.response.ActivatedJob;
//import io.camunda.zeebe.client.api.worker.JobClient;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//public class TemperatureTake {
//
//    private final RabbitTemplate rabbitTemplate;
//    private String temperatureResponse;
//    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON parser
//    public static final String EXCHANGE_NAME = "ExternalSystem.exchange";
//
//    public TemperatureTake(RabbitTemplate rabbitTemplate) {
//        this.rabbitTemplate = rabbitTemplate;
//    }
//
//    @ZeebeWorker(type = "temperaturetaking", autoComplete = false)
//    public void temperaturetaking(JobClient client, ActivatedJob job) throws JsonProcessingException {
//
//        System.out.println("Requesting temperature from external system...");
//
//        //ToDo - Whats the point of having requestMessage from the fact that the routing key is sufficient for the request?
//        String requestMessage = "{\"request\":\"fetchTemperature\"}";
//        rabbitTemplate.convertAndSend(EXCHANGE_NAME, "temperature.request", requestMessage);
//
//        System.out.println("Temperature request sent to message broker.");
//
//        // Waiting for the response from the adaptor
//        while (temperatureResponse == null) {
//            System.out.println("Waiting for temperature response...");
//            try {
//                Thread.sleep(1000);  // Polling interval
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//
//        // Parse the JSON response
//        Map<String, Object> responseMap = objectMapper.readValue(temperatureResponse, Map.class);
//
//        // Extract the temperature value
//        Integer temperature = (Integer) responseMap.get("value");
//
//        // Print the extracted value
//        System.out.println("Extracted Temperature Value: " + temperature);
//
//        // Complete the job with the extracted temperature
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("temperature", temperature);
//
//        client.newCompleteCommand(job.getKey())
//                .variables(variables)
//                .send()
//                .join();
//
//
//        // Reset the response for the next run
//        temperatureResponse = null;
//    }
//
//    @RabbitListener(queues = "temperature.response.queue")
//    public void receiveTemperatureResponse(String message) {
//        System.out.println("Received temperature response from Adaptor: " + message);
//        this.temperatureResponse = message;
//    }
//}



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@Component
public class TemperatureTake {

    private final RabbitTemplate rabbitTemplate;
    private final ZeebeClient zeebeClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DynamicQueueManager dynamicQueueManager;

    public static final String REQUEST_QUEUE = "temperature.request.queue";
    public static final String RESPONSE_QUEUE = "temperature.response.queue";
    public static final String REQUEST_ROUTING_KEY = "temperature.request";
    public static final String RESPONSE_ROUTING_KEY = "temperature.response";

    private Long latestJobKey;

    @Autowired
    public TemperatureTake(RabbitTemplate rabbitTemplate, @Qualifier("zeebeClientLifecycle") ZeebeClient zeebeClient, DynamicQueueManager dynamicQueueManager) {
        this.rabbitTemplate = rabbitTemplate;
        this.zeebeClient = zeebeClient;
        this.dynamicQueueManager = dynamicQueueManager;

        // Ensure queues and bindings are created
        dynamicQueueManager.createQueueAndBinding(REQUEST_QUEUE, REQUEST_ROUTING_KEY);
        dynamicQueueManager.createQueueAndBinding(RESPONSE_QUEUE, RESPONSE_ROUTING_KEY);
    }

    @ZeebeWorker(type = "temperaturetaking", autoComplete = false)
    public void temperaturetaking(JobClient client, ActivatedJob job) throws JsonProcessingException {

        System.out.println("Requesting temperature from external system...");

        this.latestJobKey = job.getKey();

        String requestMessage = "{\"request\":\"fetchTemperature\", \"jobKey\": " + job.getKey() + "}";
        rabbitTemplate.convertAndSend(DynamicQueueManager.EXCHANGE_NAME, REQUEST_ROUTING_KEY, requestMessage);

        System.out.println("Temperature request sent to message broker.");
    }

    @RabbitListener(queues = RESPONSE_QUEUE)
    public void receiveTemperatureResponse(String message) throws JsonProcessingException {

        System.out.println("Received temperature response from Adaptor: " + message);

        Map<String, Object> responseMap = objectMapper.readValue(message, Map.class);
        Integer temperature = (Integer) responseMap.get("value");

        System.out.println("Extracted Temperature Value: " + temperature);

        if (latestJobKey == null) {
            System.err.println("No job key available to complete the process. Ignoring the response.");
            return;
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("temperature", temperature);

            zeebeClient.newCompleteCommand(latestJobKey)
                    .variables(variables)
                    .send()
                    .join();

            System.out.println("Temperature value sent to BPMN and marked as complete.");
            latestJobKey = null;

        } catch (Exception e) {
            System.err.println("Failed to complete the job: " + e.getMessage());
        }
    }
}


