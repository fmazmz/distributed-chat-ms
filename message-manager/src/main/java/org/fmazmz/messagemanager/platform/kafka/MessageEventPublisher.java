package org.fmazmz.messagemanager.platform.kafka;

import org.fmazmz.messagemanager.event.MessageSentEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageEventPublisher extends KafkaService {

    public MessageEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    public void publishMessageSent(MessageSentEvent event) {
        publish(Topics.MESSAGE_EVENT_TOPIC, event.chatSessionId().toString(), event);
    }
}
