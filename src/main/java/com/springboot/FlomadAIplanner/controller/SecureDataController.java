package com.springboot.FlomadAIplanner.controller;

import com.springboot.FlomadAIplanner.service.EncryptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SecureDataController {

    private final EncryptionService encryptionService;

    public SecureDataController(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    @PostMapping("/encrypt")
    public ResponseEntity<String> encryptData(@RequestBody String data) {
        try {
            String encryptedData = encryptionService.encryptSensitiveData(data);
            return ResponseEntity.ok(encryptedData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/decrypt")
    public ResponseEntity<String> decryptData(@RequestBody String encryptedData) {
        try {
            String decryptedData = encryptionService.decryptSensitiveData(encryptedData);
            return ResponseEntity.ok(decryptedData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

