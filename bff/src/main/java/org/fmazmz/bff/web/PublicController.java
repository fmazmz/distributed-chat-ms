package org.fmazmz.bff.web;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    @GetMapping("/health")
    public Map<String, String> health() {
        log.info("BFF health check");
        return Map.of("service", "bff", "status", "ok");
    }
}
