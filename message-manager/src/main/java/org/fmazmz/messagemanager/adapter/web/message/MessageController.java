package org.fmazmz.messagemanager.adapter.web.message;

import jakarta.validation.Valid;
import org.fmazmz.messagemanager.adapter.web.ApiResponse;
import org.fmazmz.messagemanager.application.message.MessageApplicationService;
import org.fmazmz.messagemanager.application.message.dto.CreateMessageRequest;
import org.fmazmz.messagemanager.application.message.dto.MessageResponse;
import org.fmazmz.messagemanager.application.message.dto.MessageSliceResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(
        path = "api/v1/rooms/{roomId}/messages",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
)
public class MessageController {

    private final MessageApplicationService messageApplicationService;

    public MessageController(MessageApplicationService messageApplicationService) {
        this.messageApplicationService = messageApplicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> postMessage(
            @PathVariable UUID roomId,
            @RequestBody @Valid CreateMessageRequest request) {
        MessageResponse created = messageApplicationService.postMessage(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<MessageSliceResponse>> listMessages(
            @PathVariable UUID roomId,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(messageApplicationService.listMessages(roomId, pageable)));
    }
}
