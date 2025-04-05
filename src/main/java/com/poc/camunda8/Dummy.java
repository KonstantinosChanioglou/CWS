package com.poc.camunda8;

import org.springframework.stereotype.Component;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;

//Publish TakeTemperature topic to the broker.
@Component
public class Dummy {
    @ZeebeWorker(type="dummyaction", autoComplete = true)
    public void dummyaction(){
        System.out.println("dummyaction");
    }
}
