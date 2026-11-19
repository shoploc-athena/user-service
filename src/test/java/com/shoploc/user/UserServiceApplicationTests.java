package com.shoploc.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import com.shoploc.user.testcontainers.TestcontainersConfiguration;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class UserServiceApplicationTests {

	@Test
	void contextLoads() {
	}
}
