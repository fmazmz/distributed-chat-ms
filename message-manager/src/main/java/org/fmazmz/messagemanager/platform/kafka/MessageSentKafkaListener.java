package org.fmazmz.messagemanager.platform.kafka;

import lombok.RequiredArgsConstructor;
import org.fmazmz.messagemanager.platform.kafka.event.MessageSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MessageSentKafkaListener {

    private final MessageEventPublisher messageEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMessageSent(MessageSentEvent event) {
        messageEventPublisher.publishMessageSent(event);
    }
}
