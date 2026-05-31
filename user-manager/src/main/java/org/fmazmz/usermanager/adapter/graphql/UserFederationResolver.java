package org.fmazmz.usermanager.adapter.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.federation.DefaultDgsFederationResolver;
import java.util.Map;
import org.fmazmz.usermanager.adapter.graphql.dto.UserView;

@DgsComponent
public class UserFederationResolver extends DefaultDgsFederationResolver {

    @Override
    public Map<Class<?>, String> typeMapping() {
        return Map.of(UserView.class, "User");
    }
}
