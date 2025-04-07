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


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;


import java.util.HashMap;
import java.util.Map;

@Component
public class TemperatureTake {

    private final RabbitTemplate rabbitTemplate;
    private String temperatureResponse;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON parser

    public TemperatureTake(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @ZeebeWorker(type = "temperaturetaking", autoComplete = false)
    public void temperaturetaking(JobClient client, ActivatedJob job) throws JsonProcessingException {

        System.out.println("Requesting temperature from external system...");

        String requestMessage = "{\"request\":\"fetchTemperature\"}";
        rabbitTemplate.convertAndSend("temperature.exchange", "temperature.request", requestMessage);

        System.out.println("Temperature request sent to message broker.");

        // Waiting for the response from the adaptor
        while (temperatureResponse == null) {
            System.out.println("Waiting for temperature response...");
            try {
                Thread.sleep(1000);  // Polling interval
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Parse the JSON response
        Map<String, Object> responseMap = objectMapper.readValue(temperatureResponse, Map.class);

        // Extract the temperature value
        Integer temperature = (Integer) responseMap.get("value");

        // Print the extracted value
        System.out.println("Extracted Temperature Value: " + temperature);

        // Complete the job with the extracted temperature
        Map<String, Object> variables = new HashMap<>();
        variables.put("temperature", temperature);

        client.newCompleteCommand(job.getKey())
                .variables(variables)
                .send()
                .join();


        // Reset the response for the next run
        temperatureResponse = null;
    }

    @RabbitListener(queues = "temperature.response.queue")
    public void receiveTemperatureResponse(String message) {
        System.out.println("Received temperature response from Adaptor: " + message);
        this.temperatureResponse = message;
    }
}
