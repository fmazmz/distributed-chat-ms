package org.fmazmz.bff.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class BffProperties {

    private String authManagerUrl = "http://localhost:8081";
    private String userManagerUrl = "http://localhost:8082";
    private String messageManagerUrl = "http://localhost:8083";
    private String messageWebSocketUrl = "ws://localhost:8083/chat";
}
