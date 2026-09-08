package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendFeeCreationEvent(Object payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.FEE_CREATION_ROUTING_KEY,
                payload
        );
    }

    public void sendFeeUpdateEvent(Object payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.FEE_UPDATE_ROUTING_KEY,
                payload
        );
    }

    public void sendStudentFeeCreationEvent(Object payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.NEW_STUDENT_FEE_ROUTING_KEY,
                payload
        );
    }

    public void sendCreateResultsEvent(Object payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.CREATE_RESULTS_ROUTING_KEY,
                payload
        );
    }

    public void sendUpdateResultsEvent(Object payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.UPDATE_RESULTS_ROUTING_KEY,
                payload
        );
    }

    public void sendDailyAttendanceEvent(Object payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.DAILY_ATTENDANCE_ROUTING_KEY,
                payload
        );
    }

    public void paymentReportBroadcast(Object payload) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.PAYMENT_REPORT_BROADCAST_KEY,
                payload
        );
    }
}
