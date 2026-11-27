package com.codewithben.schoolmanagementsystem.Service;

import com.codewithben.schoolmanagementsystem.Config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Slf4j
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
     * Publishes once the caller's database transaction has committed.
     *
     * Every consumer re-loads the records by id. Published inside the transaction, a fast
     * consumer could run before the commit and not see the new or changed rows: a newly
     * enrolled student was then reported as "Invalid student id" and never billed, and
     * results created by attendance could be missed. Outside a transaction it publishes
     * straight away, as before.
     *
     * Trade-off: if the broker is unreachable after the commit, the database change stays
     * and the background work is skipped; this is logged as an error with the event's
     * correlation id so it can be replayed. Before, the whole request failed instead.
     */
    private void send(String routingKey, Object payload) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        publish(routingKey, payload);
                    } catch (RuntimeException e) {
                        log.error("Could not publish {} after commit; the change is saved but its background work did not run",
                                routingKey, e);
                    }
                }
            });
            return;
        }
        publish(routingKey, payload);
    }

    /**
     * Publishes to the main exchange with a correlation id ("routingKey:uuid"),
     * so a failed publisher confirm in the logs identifies which event was lost.
     */
    private void publish(String routingKey, Object payload) {
        CorrelationData correlation = new CorrelationData(routingKey + ":" + UUID.randomUUID());
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, payload, correlation);
    }
}
