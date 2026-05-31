package org.fmazmz.usermanager.adapter.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsEntityFetcher;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.fmazmz.usermanager.adapter.graphql.dto.UserView;
import org.fmazmz.usermanager.application.UserDetailService;
import org.fmazmz.usermanager.model.User;
import org.fmazmz.usermanager.security.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@DgsComponent
@RequiredArgsConstructor
public class UserDataFetcher {

    private final UserDetailService userDetailService;
    private final JwtUtils jwtUtils;

    @DgsQuery
    public UserView me() {
        return toView(userDetailService.getUserById(jwtUtils.currentUserId()));
    }

    @DgsQuery
    public UserView user(@InputArgument UUID id) {
        assertSelfOnly(id);
        return toView(userDetailService.getUserById(id));
    }

    @DgsEntityFetcher(name = "User")
    public UserView userEntity(Map<String, Object> values) {
        String id = (String) values.get("id");
        return toView(userDetailService.getUserById(UUID.fromString(id)));
    }

    private void assertSelfOnly(UUID profileId) {
        if (!jwtUtils.currentUserId().equals(profileId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "JWT subject must match the requested user id");
        }
    }

    private static UserView toView(User user) {
        return new UserView(
                user.getId().toString(), user.getUserName(), user.getEmail());
    }
}
