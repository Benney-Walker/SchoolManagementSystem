package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Config.RabbitMQConfig;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.FeeCreation;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Fees.FeesUpdate;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Results.CreateResults;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Results.ResultsUpdate;
import com.codewithben.schoolmanagementsystem.DTO.RabbitMQ.Student.NewStudentFee;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RabbitMQConsumer {

    private final AsyncService asyncService;

    @RabbitListener(queues = RabbitMQConfig.FEE_CREATION_QUEUE_NAME)
    public void consumeFeeCreationEvent(FeeCreation feeCreation) {
        asyncService.createIndividualFeeRecord(feeCreation);
    }

    @RabbitListener(queues = RabbitMQConfig.FEE_UPDATE_QUEUE_NAME)
    public void consumeFeeUpdateEvent(FeesUpdate feesUpdate) {
        asyncService.updateIndividualFeeRecord(feesUpdate);
    }

    @RabbitListener(queues = RabbitMQConfig.NEW_STUDENT_FEE_CREATION_QUEUE_NAME)
    public void consumeStudentFeeCreationEvent(NewStudentFee newStudentFee) {
        asyncService.createNewStudentFee(newStudentFee);
    }

    @RabbitListener(queues = RabbitMQConfig.CREATE_RESULTS_QUEUE_NAME)
    public void consumeResultsCreationEvent(CreateResults createResults) {
        asyncService.create_updateResults(createResults);
    }

    @RabbitListener(queues = RabbitMQConfig.UPDATE_RESULTS_QUEUE_NAME)
    public void consumeUpdateResultsEvent(ResultsUpdate resultsUpdate) {
        asyncService.updateResults(resultsUpdate);
    }
}
