package com.comp5348.store.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // exchange
    public static final String BANK_EXCHANGE = "bank.exchange";
    public static final String DELIVERY_EXCHANGE = "delivery.exchange";
    public static final String EMAIL_EXCHANGE = "email.exchange";
    public static final String STATUS_EXCHANGE = "status.exchange";

    // queue
    public static final String BANK_QUEUE = "bank.queue";
    public static final String BANK_REPLY_QUEUE = "bank.reply.queue";
    public static final String DELIVERY_QUEUE = "delivery.queue";
    public static final String EMAIL_QUEUE = "email.queue";
    public static final String STATUS_DELIVERY_QUEUE = "status.delivery.queue";

    // routing keys
    public static final String RK_BANK_REQUEST = "bank.request";
    public static final String RK_DELIVERY_REQUEST = "delivery.request";
    public static final String RK_EMAIL_REQUEST = "email.request";
    public static final String RK_DELIVERY_STATUS = "delivery.status";

    // exchange usage
    @Bean
    public TopicExchange bankExchange() {
        return ExchangeBuilder.topicExchange(BANK_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange deliveryExchange() {
        return ExchangeBuilder.topicExchange(DELIVERY_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange emailExchange() {
        return ExchangeBuilder.topicExchange(EMAIL_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange statusExchange() {
        return ExchangeBuilder.topicExchange(STATUS_EXCHANGE).durable(true).build();
    }

    // queue usage
    @Bean public Queue bankQueue()        { return QueueBuilder.durable(BANK_QUEUE).build(); }
    @Bean public Queue bankReplyQueue()   { return QueueBuilder.durable(BANK_REPLY_QUEUE).build(); }
    @Bean public Queue deliveryQueue()    { return QueueBuilder.durable(DELIVERY_QUEUE).build(); }
    @Bean public Queue emailQueue()       { return QueueBuilder.durable(EMAIL_QUEUE).build(); }
    @Bean public Queue statusQueue()      { return QueueBuilder.durable(STATUS_DELIVERY_QUEUE).build(); }

    // bindings
    @Bean public Binding bankBinding() {
        return BindingBuilder.bind(bankQueue()).to(bankExchange()).with(RK_BANK_REQUEST);
    }
    @Bean public Binding deliveryBinding() {
        return BindingBuilder.bind(deliveryQueue()).to(deliveryExchange()).with(RK_DELIVERY_REQUEST);
    }
    @Bean public Binding emailBinding() {
        return BindingBuilder.bind(emailQueue()).to(emailExchange()).with(RK_EMAIL_REQUEST);
    }
    @Bean public Binding statusBinding() {
        return BindingBuilder.bind(statusQueue()).to(statusExchange()).with(RK_DELIVERY_STATUS);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RPC
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setReplyAddress(BANK_REPLY_QUEUE);
        template.setReceiveTimeout(10000);            // 10s timeout
        return template;
    }

    // listen container
    @Bean
    public SimpleMessageListenerContainer replyListenerContainer(ConnectionFactory connectionFactory,
                                                                 RabbitTemplate rabbitTemplate) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(BANK_REPLY_QUEUE);
        container.setMessageListener(rabbitTemplate);
        return container;
    }

    @Bean
    public TopicExchange cancelExchange() {
        return new TopicExchange("cancel.exchange", true, false);
    }

}
