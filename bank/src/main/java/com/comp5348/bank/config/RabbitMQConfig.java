package com.comp5348.bank.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BANK_EXCHANGE = "bank.exchange";
    public static final String BANK_QUEUE = "bank.queue";
    public static final String BANK_REPLY_QUEUE = "bank.reply.queue";
    public static final String RK_BANK_REQUEST = "bank.request";

    // For outbox pattern notifications
    public static final String BANK_STATUS_EXCHANGE = "bank.status.exchange";
    public static final String RK_BANK_STATUS = "bank.status";

    @Bean
    public TopicExchange bankExchange() {
        return ExchangeBuilder.topicExchange(BANK_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue bankQueue() {
        return QueueBuilder.durable(BANK_QUEUE).build();
    }

    @Bean
    public Queue bankReplyQueue() {
        return QueueBuilder.durable(BANK_REPLY_QUEUE).build();
    }

    @Bean
    public Binding bankBinding() {
        return BindingBuilder.bind(bankQueue()).to(bankExchange()).with(RK_BANK_REQUEST);
    }

    // Outbox pattern exchange
    @Bean
    public TopicExchange bankStatusExchange() {
        return ExchangeBuilder.topicExchange(BANK_STATUS_EXCHANGE).durable(true).build();
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}