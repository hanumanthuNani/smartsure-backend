package com.smartsure.claims.config;

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

    public static final String EXCHANGE_NAME = "claims.exchange";
    public static final String QUEUE_NAME = "claims.queue";
    public static final String ROUTING_KEY = "claim.#";

    @Bean
    public TopicExchange claimsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue claimsQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding claimBinding(Queue claimsQueue, TopicExchange claimsExchange) {
        return BindingBuilder.bind(claimsQueue).to(claimsExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter());
        return template;
    }
}