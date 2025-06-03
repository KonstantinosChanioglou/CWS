package com.poc.camunda8;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Configuration
@Service
public class DynamicQueueManager {

    public static final String EXCHANGE_NAME = "ExternalSystem.exchange";

    private final RabbitAdmin rabbitAdmin;

    @Autowired
    public DynamicQueueManager(@Lazy RabbitAdmin rabbitAdmin) {  // Use @Lazy here to break circular dependency
        this.rabbitAdmin = rabbitAdmin;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public TopicExchange topicExchange() {
        TopicExchange exchange = new TopicExchange(EXCHANGE_NAME);
        rabbitAdmin.declareExchange(exchange);
        System.out.println("Exchange created: " + EXCHANGE_NAME);
        return exchange;
    }

    public void createQueueAndBinding(String queueName, String routingKey) {
        Queue queue = new Queue(queueName, true);
        rabbitAdmin.declareQueue(queue);

        Binding binding = BindingBuilder.bind(queue).to(topicExchange()).with(routingKey);
        rabbitAdmin.declareBinding(binding);

        System.out.println("Created Queue: " + queueName + " | Bound to Exchange: " + EXCHANGE_NAME + " with Routing Key: " + routingKey);
    }
}
