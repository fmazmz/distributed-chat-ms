package org.fmazmz.messagemanager;

import org.junit.jupiter.api.Test;
import org.fmazmz.messagemanager.platform.kafka.Topics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@EmbeddedKafka(partitions = 1, topics = Topics.MESSAGE_PUBLISHED_TOPIC)
@SpringBootTest(properties = "messagemanager.user-grpc.validate-sender=false")
@DirtiesContext
class MessageManagerApplicationTests {

	@Test
	void contextLoads() {
	}

}
