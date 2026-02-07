package com.comp5348.deliveryco.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DELIVERY_EXCHANGE = "delivery.exchange";
    public static final String DELIVERY_QUEUE = "delivery.queue";
    public static final String RK_DELIVERY_REQUEST = "delivery.request";

    public static final String STATUS_EXCHANGE = "status.exchange";
    public static final String RK_DELIVERY_STATUS = "delivery.status";

    public static final String CANCEL_EXCHANGE = "cancel.exchange";
    public static final String CANCEL_QUEUE = "cancel.delivery.queue";
    public static final String RK_ORDER_CANCELLED = "order.cancelled";

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }

    @Bean
    public TopicExchange deliveryExchange() {
        return ExchangeBuilder.topicExchange(DELIVERY_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue deliveryQueue() {
        return QueueBuilder.durable(DELIVERY_QUEUE).build();
    }

    @Bean
    public Binding deliveryBinding() {
        return BindingBuilder.bind(deliveryQueue()).to(deliveryExchange()).with(RK_DELIVERY_REQUEST);
    }

    // Used to send status updates back to the Store
    @Bean
    public TopicExchange statusExchange() {
        return ExchangeBuilder.topicExchange(STATUS_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange cancelExchange() {
        return ExchangeBuilder.topicExchange(CANCEL_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue cancelQueue() {
        return QueueBuilder.durable(CANCEL_QUEUE).build();
    }

    @Bean
    public Binding cancelBinding() {
        return BindingBuilder.bind(cancelQueue())
                .to(cancelExchange())
                .with(RK_ORDER_CANCELLED);
    }
}
