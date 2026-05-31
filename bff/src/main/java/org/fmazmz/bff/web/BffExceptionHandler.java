package org.fmazmz.bff.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@RestControllerAdvice
public class BffExceptionHandler {

    @ExceptionHandler(RestClientResponseException.class)
    ProblemDetail downstreamFailed(RestClientResponseException ex) {
        log.error("Downstream call failed: status={} body={}", ex.getStatusCode(), ex.getResponseBodyAsString());
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setTitle("Downstream service error");
        problem.setDetail(extractDetail(ex.getResponseBodyAsString(), status.value()));
        return problem;
    }

    @ExceptionHandler(ResourceAccessException.class)
    ProblemDetail backendUnavailable(ResourceAccessException ex) {
        log.error("Downstream unreachable: {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setTitle("Backend unavailable");
        problem.setDetail(
                "Could not reach a backend service (is user-manager running?). Registration may have partially completed in auth-manager.");
        return problem;
    }

    private static String extractDetail(String body, int statusCode) {
        if (body == null || body.isBlank()) {
            return "A backend service returned " + statusCode;
        }
        String error = jsonField(body, "error");
        if (error != null) {
            return error;
        }
        String detail = jsonField(body, "detail");
        if (detail != null) {
            return detail;
        }
        String message = jsonField(body, "message");
        if (message != null) {
            return message;
        }
        return body.length() > 200 ? body.substring(0, 200) : body;
    }

    private static String jsonField(String body, String field) {
        String marker = "\"" + field + "\":\"";
        int start = body.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = body.indexOf('"', start);
        if (end < 0) {
            return null;
        }
        return body.substring(start, end);
    }
}
