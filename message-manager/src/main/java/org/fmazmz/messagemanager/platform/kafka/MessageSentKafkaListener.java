package org.fmazmz.messagemanager.platform.kafka;

import lombok.RequiredArgsConstructor;
import org.fmazmz.messagemanager.platform.kafka.event.MessageSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Publishes to Kafka only after the message row is committed, so consumers never see events for rolled-back writes.
 */
@Component
@RequiredArgsConstructor
public class MessageSentKafkaListener {

    private final MessageEventPublisher messageEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMessageSent(MessageSentEvent event) {
        messageEventPublisher.publishMessageSent(event);
    }
}
