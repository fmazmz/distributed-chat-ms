package org.fmazmz.bff.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("service", "bff", "status", "ok");
    }
}
