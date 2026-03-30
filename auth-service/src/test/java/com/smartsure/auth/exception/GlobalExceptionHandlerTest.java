package com.smartsure.auth.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.smartsure.auth.dto.ApiResponse;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void handleRuntimeException_returns400() {
        RuntimeException ex = new RuntimeException("Test error");
        ResponseEntity<?> response = exceptionHandler.handleRuntimeException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleValidationException_returns400() throws NoSuchMethodException {
        BindException bindException = new BindException(new Object(), "object");
        bindException.addError(new FieldError("object", "email", "invalid"));
        
        java.lang.reflect.Method method = this.getClass().getDeclaredMethod("handleValidationException_returns400");
        org.springframework.core.MethodParameter parameter = new org.springframework.core.MethodParameter(method, -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindException);
        
        ResponseEntity<?> response = exceptionHandler.handleValidationException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleGenericException_returns500() {
        // Arrange
        Exception ex = new Exception("Unexpected error");

        // Act - Use explicit typing instead of the wildcard <?>
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGenericException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "Status should be 500");
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().isSuccess());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
