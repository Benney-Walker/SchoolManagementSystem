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
                RabbitMQConfig.FEE_ROUTING_KEY,
                payload
        );
    }
}
