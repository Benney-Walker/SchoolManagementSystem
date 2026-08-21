package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;


}
