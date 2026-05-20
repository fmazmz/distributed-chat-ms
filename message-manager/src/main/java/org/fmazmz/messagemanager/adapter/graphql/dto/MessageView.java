package org.fmazmz.messagemanager.adapter.graphql.dto;

import lombok.Value;

import java.util.UUID;

@Value
public class MessageView {
    String id;
    String chatSessionId;
    UUID senderId;
    String content;
    String createdAt;
}
