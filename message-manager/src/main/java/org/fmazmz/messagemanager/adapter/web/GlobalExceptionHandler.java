package org.fmazmz.messagemanager.adapter.web;

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
}
