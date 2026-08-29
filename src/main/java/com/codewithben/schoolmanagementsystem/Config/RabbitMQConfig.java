package com.codewithben.schoolmanagementsystem.Config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "school_management_exchange";


    /************************************************
                FINANCES_QUEUES_DECLARATIONS
     *************************************************/
    public static final String FEE_CREATION_QUEUE_NAME = "fee_creation_queue";
    public static final String FEE_UPDATE_QUEUE_NAME = "fee_update_queue";
    public static final String NEW_STUDENT_FEE_CREATION_QUEUE_NAME = "new_student_fee_creation_queue";

    /************************************************
             ACADEMIC_QUEUES_DECLARATIONS
     *************************************************/
    public static final String UPDATE_RESULTS_QUEUE_NAME = "update_results_queue";


    /************************************************
                    ROUTING
     *************************************************/
    public static final String FEE_CREATION_ROUTING_KEY = "fee_routing_key";
    public static final String FEE_UPDATE_ROUTING_KEY = "fee_update_routing_key";
    public static final String NEW_STUDENT_FEE_ROUTING_KEY = "new_student_routing_key";
    public static final String UPDATE_RESULTS_ROUTING_KEY = "update_results_routing_key";

    /************************************************
     * *******           QUEUES   ******************
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
    public Queue updateResultsQueue() {
        return new Queue(UPDATE_RESULTS_QUEUE_NAME, true);
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
    public Binding updateResultsBinding(Queue updateResultsQueue, TopicExchange exchange) {
        return BindingBuilder.bind(updateResultsQueue).to(exchange).with(UPDATE_RESULTS_ROUTING_KEY);
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

    @Bean
    public AmqpTemplate customRabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
