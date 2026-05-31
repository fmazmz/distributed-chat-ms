package org.fmazmz.messagemanager.adapter.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.federation.DefaultDgsFederationResolver;
import java.util.Map;
import org.fmazmz.messagemanager.adapter.graphql.dto.MessageView;
import org.fmazmz.messagemanager.adapter.graphql.dto.UserRef;

@DgsComponent
public class MessageFederationResolver extends DefaultDgsFederationResolver {

    @Override
    public Map<Class<?>, String> typeMapping() {
        return Map.of(
                MessageView.class, "Message",
                UserRef.class, "User");
    }
}
