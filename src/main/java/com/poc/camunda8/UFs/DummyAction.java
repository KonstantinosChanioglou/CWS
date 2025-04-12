package com.poc.camunda8.UFs;

import org.springframework.stereotype.Component;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

//Publish TakeTemperature topic to the broker.
@Component
public class DummyAction {
    @ZeebeWorker(type="executeDummyAction", autoComplete = true)
    public void executeDummyAction(){
        System.out.println("dummyaction");
    }
}
