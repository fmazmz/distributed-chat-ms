package org.fmazmz.usermanager.user.grpc;

import io.grpc.Status;
import org.fmazmz.usermanager.grpc.v1.GetUserRequest;
import org.fmazmz.usermanager.grpc.v1.GetUserResponse;
import org.fmazmz.usermanager.grpc.v1.UserManagerGrpc;
import org.fmazmz.usermanager.user.UserDetailService;
import org.fmazmz.usermanager.user.web.dto.UserDetailsRequest;
import org.fmazmz.usermanager.user.web.dto.UserDetailsResponse;
import org.fmazmz.usermanager.user.exception.UserNotFoundException;
import org.springframework.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

@GrpcService
public class UserGrpcService extends UserManagerGrpc.UserManagerImplBase {

    private final UserDetailService userDetailService;

    public UserGrpcService(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        try {
            UUID id = UUID.fromString(request.getId());
            UserDetailsResponse details =
                    userDetailService.getUserDetails(new UserDetailsRequest(id));
            GetUserResponse reply =
                    GetUserResponse.newBuilder().setUserName(details.userName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();

        } catch (IllegalArgumentException ex) {
            responseObserver.onError(
                    Status.INVALID_ARGUMENT.withDescription("id must be a valid UUID").asRuntimeException());
        } catch (UserNotFoundException ex) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(ex.getMessage()).asRuntimeException());
        }
    }
}
