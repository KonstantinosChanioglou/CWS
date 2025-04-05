package com.poc.camunda8;

import org.springframework.stereotype.Component;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

//Publish TakeTemperature topic to the broker.
@Component
public class TemperatureTake {
    @ZeebeWorker(type="temperaturetaking", autoComplete = true)
    public void temperaturetaking(){
        System.out.println("temperaturetaking");
    }
}
