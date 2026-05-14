package org.fmazmz.authmanager;

import org.fmazmz.authmanager.config.AuthProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AuthProperties.class)
public class AuthManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthManagerApplication.class, args);
	}

}
