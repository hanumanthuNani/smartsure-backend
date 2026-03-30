package com.smartsure.policy.exception;

import com.smartsure.policy.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void handleResourceNotFoundException_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not Found");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResourceNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse<Void> body = Objects.requireNonNull(response.getBody());
        assertEquals("Not Found", body.getMessage());
    }

    @Test
    void handleValidationException_returns400() {
        BindException bindException = new BindException(new Object(), "object");
        bindException.addError(new org.springframework.validation.FieldError("object", "field", "message"));
        
        MethodArgumentNotValidException ex = org.mockito.Mockito.mock(MethodArgumentNotValidException.class);
        org.mockito.BDDMockito.given(ex.getBindingResult()).willReturn(bindException);
        org.mockito.BDDMockito.given(ex.getMessage()).willReturn("Validation failed");
        
        ResponseEntity<ApiResponse<Map<String, String>>> response = exceptionHandler.handleValidationException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<Map<String, String>> body = Objects.requireNonNull(response.getBody());
        Map<String, String> data = Objects.requireNonNull(body.getData());
        assertTrue(data.containsKey("field"));
    }

    @Test
    void handleRuntimeException_returns400() {
        RuntimeException ex = new RuntimeException("Error");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleRuntimeException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<Void> body = Objects.requireNonNull(response.getBody());
        assertEquals("Error", body.getMessage());
    }

    @Test
    void handleGenericException_returns500() {
        Exception ex = new Exception("Fatal");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiResponse<Void> body = Objects.requireNonNull(response.getBody());
        assertEquals("Internal server error", body.getMessage());
    }
}
