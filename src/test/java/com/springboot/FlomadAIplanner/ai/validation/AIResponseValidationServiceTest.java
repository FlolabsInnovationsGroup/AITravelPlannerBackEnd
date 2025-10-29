package com.springboot.FlomadAIplanner.ai.validation;

import com.springboot.FlomadAIplanner.ai.dto.AIMessageResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AIResponseValidationService
 * Tests the JSON schema validation functionality for AI outputs
 */
@ExtendWith(MockitoExtension.class)
class AIResponseValidationServiceTest {

    @Mock
    private Validator validator;

    private AIResponseValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new AIResponseValidationService(validator);
    }

    @Test
    void validateResponse_WithValidResponse_ShouldReturnSuccess() {
        // Given
        AIMessageResponse response = new AIMessageResponse();
        response.setMessage("Valid travel suggestion");
        response.setSuggestions(Arrays.asList("Visit Paris", "Book a hotel"));

        when(validator.validate(any(AIMessageResponse.class)))
                .thenReturn(Collections.emptySet());

        // When
        ValidationResult result = validationService.validateResponse(response);

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrorMessage()).isNull();
        assertThat(result.getViolations()).isEmpty();
    }

    @Test
    void validateResponse_WithInvalidResponse_ShouldReturnFailure() {
        // Given
        AIMessageResponse response = new AIMessageResponse();
        response.setMessage(null); // Invalid - should not be null
        response.setSuggestions(Collections.emptyList()); // Invalid - should not be empty

        ConstraintViolation<AIMessageResponse> violation1 = createMockViolation("message", "Message cannot be null or empty", null);
        ConstraintViolation<AIMessageResponse> violation2 = createMockViolation("suggestions", "Suggestions list cannot be empty", Collections.emptyList());
        
        Set<ConstraintViolation<AIMessageResponse>> violations = new HashSet<>(Arrays.asList(violation1, violation2));
        when(validator.validate(any(AIMessageResponse.class))).thenReturn(violations);

        // When
        ValidationResult result = validationService.validateResponse(response);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).isNotNull();
        assertThat(result.getErrorMessage()).contains("Message cannot be null or empty");
        assertThat(result.getErrorMessage()).contains("Suggestions list cannot be empty");
        assertThat(result.getViolations()).hasSize(2);
    }

    @Test
    void validateAndThrow_WithValidResponse_ShouldNotThrow() throws AIResponseValidationException {
        // Given
        AIMessageResponse response = new AIMessageResponse();
        response.setMessage("Valid message");
        response.setSuggestions(Arrays.asList("Valid suggestion"));

        when(validator.validate(any(AIMessageResponse.class)))
                .thenReturn(Collections.emptySet());

        // When & Then
        validationService.validateAndThrow(response); // Should not throw
    }

    @Test
    void validateAndThrow_WithInvalidResponse_ShouldThrowException() {
        // Given
        AIMessageResponse response = new AIMessageResponse();
        response.setMessage(""); // Invalid - empty string
        response.setSuggestions(null); // Invalid - null

        ConstraintViolation<AIMessageResponse> violation = createMockViolation("message", "Message cannot be null or empty", "");
        Set<ConstraintViolation<AIMessageResponse>> violations = Collections.singleton(violation);
        when(validator.validate(any(AIMessageResponse.class))).thenReturn(violations);

        // When & Then
        assertThatThrownBy(() -> validationService.validateAndThrow(response))
                .isInstanceOf(AIResponseValidationException.class)
                .hasMessageContaining("AI response validation failed")
                .hasMessageContaining("Message cannot be null or empty");
    }

    @Test
    void validateWithContext_WithValidResponse_ShouldLogSuccessAndReturnValid() {
        // Given
        AIMessageResponse response = new AIMessageResponse();
        response.setMessage("Context-based validation test");
        response.setSuggestions(Arrays.asList("Suggestion 1", "Suggestion 2"));

        when(validator.validate(any(AIMessageResponse.class)))
                .thenReturn(Collections.emptySet());

        // When
        ValidationResult result = validationService.validateWithContext(response, "test-session-123", "Chat Response");

        // Then
        assertThat(result.isValid()).isTrue();
        assertThat(result.getErrorMessage()).isNull();
    }

    @Test
    void validateWithContext_WithInvalidResponse_ShouldLogErrorAndReturnInvalid() {
        // Given
        AIMessageResponse response = new AIMessageResponse();
        response.setMessage("   "); // Invalid - only whitespace
        response.setSuggestions(Arrays.asList("")); // Invalid - contains empty suggestion

        ConstraintViolation<AIMessageResponse> violation = createMockViolation("message", "Message cannot be blank", "   ");
        Set<ConstraintViolation<AIMessageResponse>> violations = Collections.singleton(violation);
        when(validator.validate(any(AIMessageResponse.class))).thenReturn(violations);

        // When
        ValidationResult result = validationService.validateWithContext(response, "test-session-456", "AI Chat Response");

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Message cannot be blank");
    }

    @Test
    void validateResponse_WithNullResponse_ShouldHandleGracefully() {
        // Given
        AIMessageResponse response = null;

        // When & Then
        assertThatThrownBy(() -> validationService.validateResponse(response))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void validateResponse_WithMessageTooLong_ShouldReturnFailure() {
        // Given
        AIMessageResponse response = new AIMessageResponse();
        String longMessage = "A".repeat(2001); // Exceeds @Size(max = 2000)
        response.setMessage(longMessage);
        response.setSuggestions(Arrays.asList("Valid suggestion"));

        ConstraintViolation<AIMessageResponse> violation = createMockViolation("message", "Message length must be between 1 and 2000 characters", longMessage);
        Set<ConstraintViolation<AIMessageResponse>> violations = Collections.singleton(violation);
        when(validator.validate(any(AIMessageResponse.class))).thenReturn(violations);

        // When
        ValidationResult result = validationService.validateResponse(response);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Message length must be between 1 and 2000 characters");
    }

    @Test
    void validateResponse_WithTooManySuggestions_ShouldReturnFailure() {
        // Given
        AIMessageResponse response = new AIMessageResponse();
        response.setMessage("Valid message");
        
        // Create 11 suggestions (exceeds @Size(max = 10))
        response.setSuggestions(Arrays.asList(
                "Suggestion 1", "Suggestion 2", "Suggestion 3", "Suggestion 4", "Suggestion 5",
                "Suggestion 6", "Suggestion 7", "Suggestion 8", "Suggestion 9", "Suggestion 10", "Suggestion 11"
        ));

        ConstraintViolation<AIMessageResponse> violation = createMockViolation("suggestions", "Suggestions list must contain between 1 and 10 items", response.getSuggestions());
        Set<ConstraintViolation<AIMessageResponse>> violations = Collections.singleton(violation);
        when(validator.validate(any(AIMessageResponse.class))).thenReturn(violations);

        // When
        ValidationResult result = validationService.validateResponse(response);

        // Then
        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrorMessage()).contains("Suggestions list must contain between 1 and 10 items");
    }

    /**
     * Helper method to create mock ConstraintViolation objects
     */
    @SuppressWarnings("unchecked")
    private ConstraintViolation<AIMessageResponse> createMockViolation(String propertyPath, String message, Object invalidValue) {
        ConstraintViolation<AIMessageResponse> violation = org.mockito.Mockito.mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(createPropertyPath(propertyPath));
        when(violation.getMessage()).thenReturn(message);
        when(violation.getInvalidValue()).thenReturn(invalidValue);
        return violation;
    }

    /**
     * Helper method to create mock PropertyPath
     */
    private jakarta.validation.Path createPropertyPath(String propertyName) {
        jakarta.validation.Path path = org.mockito.Mockito.mock(jakarta.validation.Path.class);
        when(path.toString()).thenReturn(propertyName);
        return path;
    }
}