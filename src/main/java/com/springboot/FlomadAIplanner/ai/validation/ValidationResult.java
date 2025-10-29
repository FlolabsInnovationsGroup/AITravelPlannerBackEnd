package com.springboot.FlomadAIplanner.ai.validation;

import jakarta.validation.ConstraintViolation;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * Result of AI response validation containing validation status and violation details
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationResult {

    private final boolean valid;
    private final String errorMessage;
    private final Set<? extends ConstraintViolation<?>> violations;

    /**
     * Creates a successful validation result
     *
     * @return ValidationResult indicating success
     */
    public static ValidationResult success() {
        return new ValidationResult(true, null, Set.of());
    }

    /**
     * Creates a failed validation result
     *
     * @param errorMessage Description of validation errors
     * @param violations Set of constraint violations
     * @return ValidationResult indicating failure
     */
    public static ValidationResult failure(String errorMessage, Set<? extends ConstraintViolation<?>> violations) {
        return new ValidationResult(false, errorMessage, violations);
    }

    /**
     * Creates a failed validation result with just an error message
     *
     * @param errorMessage Description of validation errors
     * @return ValidationResult indicating failure
     */
    public static ValidationResult failure(String errorMessage) {
        return new ValidationResult(false, errorMessage, Set.of());
    }

    /**
     * @return true if validation passed, false otherwise
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * @return true if validation failed, false otherwise
     */
    public boolean hasErrors() {
        return !valid;
    }

    /**
     * @return number of constraint violations
     */
    public int getViolationCount() {
        return violations != null ? violations.size() : 0;
    }
}