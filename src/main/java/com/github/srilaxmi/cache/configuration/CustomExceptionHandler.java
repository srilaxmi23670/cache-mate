package com.github.srilaxmi.cache.configuration;


import com.github.srilaxmi.cache.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;


@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

    private static final String INVALID_REQUEST_EXCEPTION = "INVALID REQUEST EXCEPTION :: ";

    @ExceptionHandler(value = {
            ServerWebInputException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleServerWebInputException(ServerWebInputException e) {

        log.error(INVALID_REQUEST_EXCEPTION, e);
        return ApiResponse.error("InvalidRequest");
    }

    @ExceptionHandler(value = NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<String> handleNullPointerException(Exception e) {

        log.error("NULL POINTER EXCEPTION :: ", e);
        return ApiResponse.error("NullPointerException");
    }

    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<String> handleValidationException(WebExchangeBindException ex) {
        BindingResult result = ex.getBindingResult();
        StringBuilder errorMessage = new StringBuilder();
        for (FieldError fieldError : result.getFieldErrors()) {
            errorMessage.append(fieldError.getDefaultMessage()).append("\n");
        }
        log.error(INVALID_REQUEST_EXCEPTION, ex);
        return ApiResponse.error(errorMessage.toString());
    }
}
