package com.smartsure.admin.exception;

import com.smartsure.admin.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void handleResourceNotFoundException_returns404() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleResourceNotFoundException(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void handleValidationException_returns400() {
        MethodArgumentNotValidException ex = org.mockito.Mockito.mock(MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = 
            org.mockito.Mockito.mock(org.springframework.validation.BindingResult.class);
        
        FieldError fieldError = new FieldError("object", "field", "default-message");
        
        org.mockito.Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
        org.mockito.Mockito.when(bindingResult.getFieldErrors()).thenReturn(java.util.Collections.singletonList(fieldError));
        
        ResponseEntity<ApiResponse<Map<String, String>>> response = 
            exceptionHandler.handleValidationException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals("default-message", response.getBody().getData().get("field"));
    }

    @Test
    void handleRuntimeException_returns400() {
        RuntimeException ex = new RuntimeException("Bad request");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleRuntimeException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleGenericException_returns500() {
        Exception ex = new Exception("Error");
        ResponseEntity<ApiResponse<Void>> response = exceptionHandler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
