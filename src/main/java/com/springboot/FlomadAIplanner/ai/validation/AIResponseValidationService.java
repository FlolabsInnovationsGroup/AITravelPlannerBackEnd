package com.springboot.FlomadAIplanner.ai.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for validating AI outputs using JSON schema validation
 * Ensures AI responses meet quality standards and structural requirements
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AIResponseValidationService {

    private final Validator validator;

    /**
     * Validates an AI response object against its schema constraints
     *
     * @param response The AI response object to validate
     * @param <T> The type of the response object
     * @return ValidationResult containing validation status and any violations
     */
    public <T> ValidationResult validateResponse(T response) {
        log.debug("🔍 Validating AI response of type: {}", response.getClass().getSimpleName());

        Set<ConstraintViolation<T>> violations = validator.validate(response);

        if (violations.isEmpty()) {
            log.debug("✅ AI response validation passed");
            return ValidationResult.success();
        } else {
            log.warn("❌ AI response validation failed with {} violations", violations.size());
            
            String violationDetails = violations.stream()
                    .map(violation -> String.format("%s: %s (invalid value: %s)", 
                            violation.getPropertyPath(), 
                            violation.getMessage(),
                            violation.getInvalidValue()))
                    .collect(Collectors.joining("; "));
            
            log.warn("🔍 Validation violations: {}", violationDetails);
            
            return ValidationResult.failure(violationDetails, violations);
        }
    }

    /**
     * Validates and throws exception if validation fails
     *
     * @param response The AI response object to validate
     * @param <T> The type of the response object
     * @throws AIResponseValidationException if validation fails
     */
    public <T> void validateAndThrow(T response) throws AIResponseValidationException {
        ValidationResult result = validateResponse(response);
        if (!result.isValid()) {
            throw new AIResponseValidationException(
                    "AI response validation failed: " + result.getErrorMessage(),
                    result.getViolations()
            );
        }
    }

    /**
     * Validates a response with contextual metadata
     *
     * @param response The AI response object to validate
     * @param sessionId The session ID for context
     * @param messageType The type of message for context
     * @param <T> The type of the response object
     * @return ValidationResult with contextual information
     */
    public <T> ValidationResult validateWithContext(T response, String sessionId, String messageType) {
        log.info("🔍 Validating AI response for session: {} (type: {})", sessionId, messageType);
        
        ValidationResult result = validateResponse(response);
        
        if (result.isValid()) {
            log.info("✅ AI response validation successful for session: {}", sessionId);
        } else {
            log.error("❌ AI response validation failed for session: {} - {}", sessionId, result.getErrorMessage());
        }
        
        return result;
    }
}