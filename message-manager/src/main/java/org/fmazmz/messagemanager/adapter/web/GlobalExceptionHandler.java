package org.fmazmz.messagemanager.adapter.web;

import org.fmazmz.messagemanager.exception.ChatSessionAccessDeniedException;
import org.fmazmz.messagemanager.exception.ChatSessionNotFoundException;
import org.fmazmz.messagemanager.exception.MessageOperationException;
import org.fmazmz.messagemanager.exception.SenderNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SenderNotFoundException.class)
    ProblemDetail handleSenderNotFound(SenderNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        detail.setTitle("Sender not found");
        return detail;
    }

    @ExceptionHandler(ChatSessionNotFoundException.class)
    ProblemDetail handleSessionNotFound(ChatSessionNotFoundException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        detail.setTitle("Chat session not found");
        return detail;
    }

    @ExceptionHandler(ChatSessionAccessDeniedException.class)
    ProblemDetail handleSessionAccessDenied(ChatSessionAccessDeniedException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        detail.setTitle("Not a participant in this chat session");
        return detail;
    }

    @ExceptionHandler(MessageOperationException.class)
    ProblemDetail handleMessageOp(MessageOperationException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Invalid message operation");
        return detail;
    }
}
