package com.snackOverFlow.config.rabbitmq;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class OrdersRabbitMQConfig {

    //  Exchange 
    public static final String ORDERS_EXCHANGE      = "orders.exchange";
    public static final String ORDERS_DLX           = "orders.dlx";

    //  Queues 
    public static final String QUEUE_PLACED         = "orders.placed.queue";
    public static final String QUEUE_STATUS         = "orders.status.queue";
    public static final String QUEUE_NOTIFICATION   = "orders.notification.queue";

    //  DLQs 
    public static final String DLQ_PLACED           = "orders.placed.dlq";
    public static final String DLQ_STATUS           = "orders.status.dlq";
    public static final String DLQ_NOTIFICATION     = "orders.notification.dlq";

    //  Routing Keys 
    public static final String RK_ORDER_PLACED      = "order.placed";
    public static final String RK_ORDER_STATUS      = "order.status.#";
    public static final String RK_ORDER_ALL         = "order.#";

    //  Exchange Beans 

    @Bean
    public TopicExchange ordersExchange() {
        return ExchangeBuilder.topicExchange(ORDERS_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange ordersDlx() {
        return ExchangeBuilder.directExchange(ORDERS_DLX).durable(true).build();
    }

    //  Queue: placed 

    @Bean
    public Queue ordersPlacedQueue() {
        return QueueBuilder.durable(QUEUE_PLACED)
                .withArgument("x-dead-letter-exchange", ORDERS_DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_PLACED)
                .build();
    }

    @Bean
    public Queue ordersPlacedDlq() {
        return QueueBuilder.durable(DLQ_PLACED).build();
    }

    @Bean
    public Binding ordersPlacedBinding(Queue ordersPlacedQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(ordersPlacedQueue).to(ordersExchange).with(RK_ORDER_PLACED);
    }

    @Bean
    public Binding ordersPlacedDlqBinding(Queue ordersPlacedDlq, DirectExchange ordersDlx) {
        return BindingBuilder.bind(ordersPlacedDlq).to(ordersDlx).with(DLQ_PLACED);
    }

    //  Queue: status 

    @Bean
    public Queue ordersStatusQueue() {
        return QueueBuilder.durable(QUEUE_STATUS)
                .withArgument("x-dead-letter-exchange", ORDERS_DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_STATUS)
                .build();
    }

    @Bean
    public Queue ordersStatusDlq() {
        return QueueBuilder.durable(DLQ_STATUS).build();
    }

    @Bean
    public Binding ordersStatusBinding(Queue ordersStatusQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(ordersStatusQueue).to(ordersExchange).with(RK_ORDER_STATUS);
    }

    @Bean
    public Binding ordersStatusDlqBinding(Queue ordersStatusDlq, DirectExchange ordersDlx) {
        return BindingBuilder.bind(ordersStatusDlq).to(ordersDlx).with(DLQ_STATUS);
    }

    //  Queue: notification 

    @Bean
    public Queue ordersNotificationQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION)
                .withArgument("x-dead-letter-exchange", ORDERS_DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_NOTIFICATION)
                .build();
    }

    @Bean
    public Queue ordersNotificationDlq() {
        return QueueBuilder.durable(DLQ_NOTIFICATION).build();
    }

    @Bean
    public Binding ordersNotificationBinding(Queue ordersNotificationQueue, TopicExchange ordersExchange) {
        return BindingBuilder.bind(ordersNotificationQueue).to(ordersExchange).with(RK_ORDER_ALL);
    }

    @Bean
    public Binding ordersNotificationDlqBinding(Queue ordersNotificationDlq, DirectExchange ordersDlx) {
        return BindingBuilder.bind(ordersNotificationDlq).to(ordersDlx).with(DLQ_NOTIFICATION);
    }

    //  Infrastructure Beans 

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) log.debug("Message confirmed by broker");
            else log.error("Message rejected by broker: {}", cause);
        });
        template.setReturnsCallback(ret ->
                log.error("Message returned unrouted: exchange={} routingKey={}", ret.getExchange(), ret.getRoutingKey())
        );
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(retryInterceptor());
        return factory;
    }

    @Bean
    public Advice retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(2000, 2.0, 10_000)
                .build();
    }
}

