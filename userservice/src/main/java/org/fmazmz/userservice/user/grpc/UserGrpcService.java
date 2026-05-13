package org.fmazmz.userservice.user.grpc;

import io.grpc.Status;
import org.fmazmz.userservice.grpc.v1.GetUserRequest;
import org.fmazmz.userservice.grpc.v1.GetUserResponse;
import org.fmazmz.userservice.grpc.v1.UserServiceGrpc;
import org.fmazmz.userservice.user.UserDetailService;
import org.fmazmz.userservice.user.api.dto.UserDetailsRequest;
import org.fmazmz.userservice.user.api.dto.UserDetailsResponse;
import org.fmazmz.userservice.user.exception.UserNotFoundException;
import org.springframework.grpc.server.service.GrpcService;
import io.grpc.stub.StreamObserver;

import java.util.UUID;

@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

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
