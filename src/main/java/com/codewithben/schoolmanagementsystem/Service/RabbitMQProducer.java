package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendFeeCreationEvent(Object payload) {
        send(RabbitMQConfig.FEE_CREATION_ROUTING_KEY, payload);
    }

    public void sendFeeUpdateEvent(Object payload) {
        send(RabbitMQConfig.FEE_UPDATE_ROUTING_KEY, payload);
    }

    public void sendStudentFeeCreationEvent(Object payload) {
        send(RabbitMQConfig.NEW_STUDENT_FEE_ROUTING_KEY, payload);
    }

    public void sendCreateResultsEvent(Object payload) {
        send(RabbitMQConfig.CREATE_RESULTS_ROUTING_KEY, payload);
    }

    public void sendUpdateResultsEvent(Object payload) {
        send(RabbitMQConfig.UPDATE_RESULTS_ROUTING_KEY, payload);
    }

    public void sendDailyAttendanceEvent(Object payload) {
        send(RabbitMQConfig.DAILY_ATTENDANCE_ROUTING_KEY, payload);
    }

    public void paymentReportBroadcast(Object payload) {
        send(RabbitMQConfig.PAYMENT_REPORT_BROADCAST_KEY, payload);
    }

    /**
     * Publishes to the main exchange with a correlation id ("routingKey:uuid"),
     * so a failed publisher confirm in the logs identifies which event was lost.
     */
    private void send(String routingKey, Object payload) {
        CorrelationData correlation = new CorrelationData(routingKey + ":" + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, payload, correlation);
    }
}
