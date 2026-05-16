package org.fmazmz.bff.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@RestControllerAdvice
public class BffExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    ProblemDetail downstreamFailed(RestClientResponseException ex) {
        log.error("Downstream call failed: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_GATEWAY);
        problem.setTitle("Downstream service error");
        problem.setDetail("A backend service returned " + ex.getStatusCode().value());
        return problem;
    }
}
