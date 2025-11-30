package com.springboot.FlomadAIplanner.controller.debug;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class CorsDebugController {
    
    @GetMapping("/cors-test")
    public ResponseEntity<Map<String, Object>> corsTest() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            
            response.put("message", "CORS test successful");
            response.put("origin", request.getHeader("Origin"));
            response.put("referer", request.getHeader("Referer"));
            response.put("userAgent", request.getHeader("User-Agent"));
            response.put("method", request.getMethod());
            response.put("timestamp", System.currentTimeMillis());
            
            System.out.println("🔍 CORS Test - Origin: " + request.getHeader("Origin"));
            System.out.println("🔍 CORS Test - Referer: " + request.getHeader("Referer"));
            System.out.println("🔍 CORS Test - Method: " + request.getMethod());
            
        } catch (Exception e) {
            response.put("error", "Failed to get request details: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/cors-test")
    public ResponseEntity<Map<String, Object>> corsTestPost(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS POST test successful");
        response.put("receivedBody", body);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}