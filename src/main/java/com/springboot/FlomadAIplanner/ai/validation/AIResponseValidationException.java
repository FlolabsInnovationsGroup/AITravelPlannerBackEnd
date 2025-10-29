package com.springboot.FlomadAIplanner.ai.validation;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * Exception thrown when AI response validation fails
 */
public class AIResponseValidationException extends Exception {

    private final Set<? extends ConstraintViolation<?>> violations;

    /**
     * Constructs a new AIResponseValidationException with the specified detail message
     *
     * @param message the detail message
     */
    public AIResponseValidationException(String message) {
        super(message);
        this.violations = Set.of();
    }

    /**
     * Constructs a new AIResponseValidationException with the specified detail message and violations
     *
     * @param message the detail message
     * @param violations the set of constraint violations that caused this exception
     */
    public AIResponseValidationException(String message, Set<? extends ConstraintViolation<?>> violations) {
        super(message);
        this.violations = violations != null ? violations : Set.of();
    }

    /**
     * Constructs a new AIResponseValidationException with the specified detail message and cause
     *
     * @param message the detail message
     * @param cause the cause
     */
    public AIResponseValidationException(String message, Throwable cause) {
        super(message, cause);
        this.violations = Set.of();
    }

    /**
     * Gets the constraint violations that caused this exception
     *
     * @return set of constraint violations
     */
    public Set<? extends ConstraintViolation<?>> getViolations() {
        return violations;
    }

    /**
     * Gets the number of constraint violations
     *
     * @return violation count
     */
    public int getViolationCount() {
        return violations.size();
    }

    /**
     * Checks if this exception has any constraint violations
     *
     * @return true if violations exist, false otherwise
     */
    public boolean hasViolations() {
        return !violations.isEmpty();
    }
}