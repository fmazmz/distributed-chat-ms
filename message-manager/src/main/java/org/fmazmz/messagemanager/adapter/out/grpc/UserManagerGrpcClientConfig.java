package org.fmazmz.messagemanager.adapter.out.grpc;

import org.fmazmz.usermanager.grpc.v1.UserManagerGrpc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.grpc.client.GrpcChannelFactory;

@Configuration
public class UserManagerGrpcClientConfig {

    @Bean
    @ConditionalOnProperty(name = "messagemanager.user-grpc.validate-sender", havingValue = "true", matchIfMissing = true)
    UserManagerGrpc.UserManagerBlockingStub userManagerBlockingStub(GrpcChannelFactory channels) {
        return UserManagerGrpc.newBlockingStub(channels.createChannel("user-manager"));
    }
}
