package org.fmazmz.messagemanager.platform.kafka;

import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.UUID;
import org.fmazmz.messagemanager.platform.kafka.event.MessageSentEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageSentKafkaListenerTest {

    @Mock
    MessageEventPublisher messageEventPublisher;

    @InjectMocks
    MessageSentKafkaListener listener;

    @Test
    void publishesToKafkaWhenEventReceived() {
        MessageSentEvent event = new MessageSentEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), Instant.now(), 12);

        listener.onMessageSent(event);

        verify(messageEventPublisher).publishMessageSent(event);
    }
}
