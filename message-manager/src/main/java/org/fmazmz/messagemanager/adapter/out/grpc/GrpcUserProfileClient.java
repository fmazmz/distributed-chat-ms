package org.fmazmz.messagemanager.adapter.out.grpc;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.fmazmz.messagemanager.application.user.UserProfilePort;
import org.fmazmz.messagemanager.domain.exception.SenderNotFoundException;
import org.fmazmz.usermanager.grpc.v1.GetUserRequest;
import org.fmazmz.usermanager.grpc.v1.UserManagerGrpc;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(name = "messagemanager.user-grpc.validate-sender", havingValue = "true", matchIfMissing = true)
public class GrpcUserProfileClient implements UserProfilePort {

    private final UserManagerGrpc.UserManagerBlockingStub userManagerStub;

    public GrpcUserProfileClient(UserManagerGrpc.UserManagerBlockingStub userManagerBlockingStub) {
        this.userManagerStub = userManagerBlockingStub;
    }

    @Override
    public void assertUserExists(UUID userId) {
        try {
            userManagerStub.getUser(GetUserRequest.newBuilder().setId(userId.toString()).build());
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new SenderNotFoundException(userId);
            }
            throw ex;
        }
    }
}
