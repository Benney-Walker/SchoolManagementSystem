package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConsumer {

    @RabbitListener(queues = RabbitMQConfig.FEE_CREATION_QUEUE_NAME)
    public void consumeFeeCreationEvent(Object payload) {

    }
}
