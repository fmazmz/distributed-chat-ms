package org.fmazmz.messagemanager.adapter.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.fmazmz.messagemanager.model.Message;
import org.fmazmz.messagemanager.adapter.graphql.dto.MessageView;
import org.fmazmz.messagemanager.adapter.graphql.dto.UserRef;
import org.fmazmz.messagemanager.security.JwtUtils;
import org.fmazmz.messagemanager.service.MessageApplicationService;

@DgsComponent
@RequiredArgsConstructor
public class MessageDataFetcher {

    private static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;

    private final MessageApplicationService messageApplicationService;
    private final JwtUtils jwtUtils;

    @DgsQuery
    public List<MessageView> sessionMessages(@InputArgument UUID sessionId) {
        UUID userId = jwtUtils.currentUserId();
        return messageApplicationService.listMessagesForParticipant(sessionId, userId).stream()
                .map(this::toView)
                .toList();
    }

    @DgsData(parentType = "Message", field = "sender")
    public UserRef sender(DgsDataFetchingEnvironment dfe) {
        MessageView message = dfe.getSource();
        return new UserRef(message.getSenderId().toString());
    }

    private MessageView toView(Message message) {
        Instant createdAt = message.getCreatedAt() != null ? message.getCreatedAt() : Instant.now();
        return new MessageView(
                message.getId().toString(),
                message.getChatSessionId().toString(),
                message.getSenderId(),
                message.getContent(),
                ISO_INSTANT.format(createdAt));
    }
}
