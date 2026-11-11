package com.codewithben.schoolmanagementsystem.Config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "school_management_exchange";

    /*
     * Dead letters: messages a consumer could not process (after retries, where
     * retries apply) are republished here with x-exception-* headers so they can
     * be inspected and replayed instead of being lost or requeued forever.
     * Republishing is used instead of x-dead-letter-exchange queue arguments
     * because RabbitMQ refuses to re-declare existing durable queues with new
     * arguments, which would break startup against the current broker.
     */
    public static final String DEAD_LETTER_EXCHANGE_NAME = "school_management_dlx";
    public static final String DEAD_LETTER_QUEUE_NAME = "school_management_dead_letter_queue";
    public static final String DEAD_LETTER_ROUTING_KEY = "dead_letter";

    /*
     * Listener container factory for consumers with non-repeatable side effects
     * (SMS). These send messages before recording them, so a retry after a late
     * failure would text parents twice; failures go straight to the dead-letter queue.
     */
    public static final String NO_RETRY_LISTENER_FACTORY = "noRetryListenerContainerFactory";


    /************************************************
                FINANCES_QUEUES_DECLARATIONS
     *************************************************/
    public static final String FEE_CREATION_QUEUE_NAME = "fee_creation_queue";
    public static final String FEE_UPDATE_QUEUE_NAME = "fee_update_queue";
    public static final String NEW_STUDENT_FEE_CREATION_QUEUE_NAME = "new_student_fee_creation_queue";
    public static final String PAYMENT_REPORT_BROADCAST_QUEUE_NAME = "payment_report_broadcast_queue";

    /************************************************
             ACADEMIC_QUEUES_DECLARATIONS
     *************************************************/
    public static final String CREATE_RESULTS_QUEUE_NAME = "create_results_queue";
    public static final String UPDATE_RESULTS_QUEUE_NAME = "update_results_queue";
    public static final String DAILY_ATTENDANCE_QUEUE_NAME = "daily_attendance_queue";


    /************************************************
                    ROUTING
     *************************************************/
    public static final String FEE_CREATION_ROUTING_KEY = "fee_routing_key";
    public static final String FEE_UPDATE_ROUTING_KEY = "fee_update_routing_key";
    public static final String NEW_STUDENT_FEE_ROUTING_KEY = "new_student_routing_key";
    public static final String CREATE_RESULTS_ROUTING_KEY = "create_results_routing";
    public static final String UPDATE_RESULTS_ROUTING_KEY = "update_results_routing_key";
    public static final String DAILY_ATTENDANCE_ROUTING_KEY = "daily_attendance_routing_key";

    public static final String PAYMENT_REPORT_BROADCAST_KEY = "payment_report_broadcast_key";

    /************************************************
     * *******       QUEUES METHODS   ***********
    *************************************************/
    @Bean
    public Queue feeCreationQueue() {
        return new Queue(FEE_CREATION_QUEUE_NAME, true);
    }

    @Bean
    public Queue feeUpdateQueue() {
        return new Queue(FEE_UPDATE_QUEUE_NAME, true);
    }

    @Bean
    public Queue newStudentFeeCreationQueue() {
        return new Queue(NEW_STUDENT_FEE_CREATION_QUEUE_NAME, true);
    }

    @Bean
    public Queue createResultsQueue() {
        return new Queue(CREATE_RESULTS_QUEUE_NAME, true);
    }

    @Bean
    public Queue updateResultsQueue() {
        return new Queue(UPDATE_RESULTS_QUEUE_NAME, true);
    }

    @Bean
    public Queue dailyAttendanceQueue() {
        return new Queue(DAILY_ATTENDANCE_QUEUE_NAME, true);
    }

    @Bean
    public Queue paymentBroadcastQueue() {
        return new Queue(PAYMENT_REPORT_BROADCAST_QUEUE_NAME, true);
    }

    /************************************************
                       BINDINGS
     *************************************************/
    @Bean
    public Binding feeCreationBinding(Queue feeCreationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(feeCreationQueue).to(exchange).with(FEE_CREATION_ROUTING_KEY);
    }

    @Bean
    public Binding feeUpdateBinding(Queue feeUpdateQueue, TopicExchange exchange) {
        return BindingBuilder.bind(feeUpdateQueue).to(exchange).with(FEE_UPDATE_ROUTING_KEY);
    }

    @Bean
    public Binding newStudentFeeCreationBinding(Queue newStudentFeeCreationQueue, TopicExchange exchange) {
        return BindingBuilder.bind(newStudentFeeCreationQueue).to(exchange).with(NEW_STUDENT_FEE_ROUTING_KEY);
    }

    @Bean
    public Binding createResultsBinding(Queue createResultsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(createResultsQueue).to(exchange).with(CREATE_RESULTS_ROUTING_KEY);
    }

    @Bean
    public Binding updateResultsBinding(Queue updateResultsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(updateResultsQueue).to(exchange).with(UPDATE_RESULTS_ROUTING_KEY);
    }

    @Bean
    public Binding dailyAttendanceBinding(Queue dailyAttendanceQueue, TopicExchange exchange) {
        return BindingBuilder.bind(dailyAttendanceQueue).to(exchange).with(DAILY_ATTENDANCE_ROUTING_KEY);
    }

    @Bean
    public Binding paymentBroadcastBinding(Queue paymentBroadcastQueue, TopicExchange exchange) {
        return BindingBuilder.bind(paymentBroadcastQueue).to(exchange).with(PAYMENT_REPORT_BROADCAST_KEY);
    }

    /************************************************
                  BROKER_STATIC_CONFIG
     *************************************************/

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Replaces Spring Boot's default template (which backs off when this bean exists).
     * The configurer applies spring.rabbitmq.template.* properties and the JSON
     * converter above; the callbacks make broker-side publish failures visible
     * instead of silently dropping the event.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(RabbitTemplateConfigurer configurer,
                                         ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate();
        configurer.configure(template, connectionFactory);

        // Broker did not accept the message (requires publisher-confirm-type=correlated).
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("RabbitMQ publish NOT confirmed [{}]: {}",
                        correlationData != null ? correlationData.getId() : "unknown", cause);
            }
        });

        // Broker accepted the message but no queue was bound to its routing key
        // (requires publisher-returns=true and template.mandatory=true).
        template.setReturnsCallback(returned ->
                log.error("RabbitMQ message unroutable: exchange={} routingKey={} reply={} {}",
                        returned.getExchange(), returned.getRoutingKey(),
                        returned.getReplyCode(), returned.getReplyText()));

        return template;
    }

    /************************************************
                  DEAD_LETTER_HANDLING
     *************************************************/

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE_NAME);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE_NAME, true);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * Picked up by Spring Boot's default listener container factory: once the
     * retry attempts configured under spring.rabbitmq.listener.simple.retry are
     * exhausted, the message is republished to the dead-letter queue.
     */
    @Bean
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, DEAD_LETTER_EXCHANGE_NAME, DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * Same settings as the default factory, but a single attempt: on failure the
     * message is dead-lettered immediately. Use for consumers whose side effects
     * must not repeat (see NO_RETRY_LISTENER_FACTORY).
     */
    @Bean(name = NO_RETRY_LISTENER_FACTORY)
    public SimpleRabbitListenerContainerFactory noRetryListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            MessageRecoverer messageRecoverer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(1)
                .recoverer(messageRecoverer)
                .build());
        return factory;
    }
}
