package com.smartsure.policy.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "policy.exchange";
    public static final String QUEUE = "policy.queue";
    public static final String ROUTING_KEY = "policy.purchase";

    @Bean
    public Queue policyQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public TopicExchange policyExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding policyBinding(Queue policyQueue, TopicExchange policyExchange) {
        return BindingBuilder.bind(policyQueue).to(policyExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
