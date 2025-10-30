package com.masa.pScheduler.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getRequestURI()).thenReturn("/api/test");
    }

    @Test
    void whenResourceNotFound_thenReturnsNotFoundResponse() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Task not found");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleResourceNotFound(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("Task not found");
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
    }

    @Test
    void whenTypeMismatch_thenReturnsBadRequestResponse() {
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                "invalid-date", LocalDateTime.class, "referenceTime", null, new IllegalArgumentException("Invalid format"));

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleTypeMismatch(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("Invalid value 'invalid-date'");
    }

    @Test
    void whenGenericException_thenReturnsInternalServerError() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleGenericException(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
    }

    @Test
    void whenValidationFails_thenReturnsBadRequestWithFieldErrors() {
        // Given a simulated validation exception with one field error
        MethodArgumentNotValidException ex = TestValidationUtils.mockValidationException("title", "Title must not be empty");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleValidationException(ex, request);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getValidationErrors())
                .containsEntry("title", "Title must not be empty");
    }

    @Test
    void whenAccessDenied_thenReturnsForbiddenResponse() {
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        ResponseEntity<ErrorResponse> response =
                exceptionHandler.handleAccessDenied(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody().getError()).isEqualTo("Forbidden");
        assertThat(response.getBody().getMessage()).isEqualTo("Access is denied");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}
