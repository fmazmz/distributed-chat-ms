package org.fmazmz.messagemanager.adapter.web.message.dto;

import org.fmazmz.messagemanager.model.Message;
import org.springframework.data.domain.Page;

import java.util.List;

public record MessageSliceResponse(
        List<MessageResponse> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        boolean last
) {
    public static MessageSliceResponse from(Page<Message> page) {
        List<MessageResponse> items = page.getContent().stream().map(MessageResponse::from).toList();
        return new MessageSliceResponse(
                items,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isLast());
    }
}
