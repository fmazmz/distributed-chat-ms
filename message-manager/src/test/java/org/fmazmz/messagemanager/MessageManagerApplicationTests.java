package org.fmazmz.messagemanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "messagemanager.user-grpc.validate-sender=false")
class MessageManagerApplicationTests {

	@Test
	void contextLoads() {
	}

}
