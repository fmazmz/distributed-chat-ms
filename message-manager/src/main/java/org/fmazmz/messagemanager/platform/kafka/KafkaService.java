package org.fmazmz.messagemanager.platform.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

public abstract class KafkaService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    protected void publishAsync(String topic, String key, Object event) {
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                logger.error(
                        "Failed to publish event to topic={}, key={}, type={}",
                        topic,
                        key,
                        event.getClass().getSimpleName(),
                        ex);
                return;
            }
            logger.info(
                    "Published event to topic={}, key={}, type={}, partition={}, offset={}",
                    topic,
                    key,
                    event.getClass().getSimpleName(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset());
        });
    }
}
