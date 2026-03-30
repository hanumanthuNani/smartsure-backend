package com.smartsure.claims.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class RabbitMQConfigTest {

    private final RabbitMQConfig rabbitMQConfig = new RabbitMQConfig();

    @Test
    void testBeans() {
        TopicExchange exchange = rabbitMQConfig.claimsExchange();
        assertNotNull(exchange);
        assertEquals(RabbitMQConfig.EXCHANGE_NAME, exchange.getName());

        Queue queue = rabbitMQConfig.claimsQueue();
        assertNotNull(queue);
        assertEquals(RabbitMQConfig.QUEUE_NAME, queue.getName());

        Binding binding = rabbitMQConfig.claimBinding(queue, exchange);
        assertNotNull(binding);
        assertEquals(RabbitMQConfig.ROUTING_KEY, binding.getRoutingKey());

        MessageConverter converter = rabbitMQConfig.jacksonMessageConverter();
        assertNotNull(converter);

        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        RabbitTemplate template = rabbitMQConfig.rabbitTemplate(connectionFactory);
        assertNotNull(template);
        assertNotNull(template.getMessageConverter());
        assertTrue(template.getMessageConverter() instanceof Jackson2JsonMessageConverter);
    }
}
