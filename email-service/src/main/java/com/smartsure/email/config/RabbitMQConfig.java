package com.smartsure.email.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue:email.queue}")
    private String queueName;

    @Value("${rabbitmq.exchange:email.exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing-key:email.routing.key}")
    private String routingKey;

    @Bean
    public Queue emailQueue() {
        return new Queue(queueName, true);
    }

    @Bean
    public Queue policyQueue() {
        return new Queue("policy.queue", true);
    }

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue emailQueue, TopicExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with(routingKey);
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
