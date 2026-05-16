package org.fmazmz.messagemanager;

import org.fmazmz.messagemanager.platform.kafka.Topics;
import org.fmazmz.messagemanager.service.UserProfilePort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@EmbeddedKafka(partitions = 1, topics = Topics.MESSAGE_PUBLISHED_TOPIC)
@SpringBootTest
@DirtiesContext
class MessageManagerApplicationTests {

    @MockitoBean
    UserProfilePort userProfilePort;

    @Test
    void contextLoads() {}
}
