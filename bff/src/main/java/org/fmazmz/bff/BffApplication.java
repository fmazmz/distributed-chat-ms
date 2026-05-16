package org.fmazmz.bff;

import org.fmazmz.bff.config.BffProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(BffProperties.class)
public class BffApplication {

    private static final Logger log = LoggerFactory.getLogger(BffApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(BffApplication.class, args);
    }

    @Bean
    ApplicationRunner startupBanner() {
        return args -> {
            log.info("BFF ready — this must be the process bound to port 8080 for the SPA");
            log.info("Smoke test: GET http://localhost:8080/api/v1/public/health should return service=bff");
        };
    }
}
