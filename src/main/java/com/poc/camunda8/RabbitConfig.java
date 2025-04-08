package com.poc.camunda8;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String SEPSIS_EXCHANGE = "sepsis.exchange";
    public static final String SEPSIS_QUEUE = "sepsis.execute.queue";
    public static final String SEPSIS_ROUTING_KEY = "sepsis.execute";

    @Bean
    public Exchange sepsisExchange() {
        return ExchangeBuilder.directExchange(SEPSIS_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue sepsisQueue() {
        return QueueBuilder.durable(SEPSIS_QUEUE).build();
    }

    @Bean
    public Binding sepsisBinding(Queue sepsisQueue, Exchange sepsisExchange) {
        return BindingBuilder.bind(sepsisQueue).to(sepsisExchange).with(SEPSIS_ROUTING_KEY).noargs();
    }
}
