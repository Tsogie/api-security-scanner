package net.tool.exception;


import lombok.extern.slf4j.Slf4j;
import net.tool.dto.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ResponseDto> handleInvalidUrl(InvalidUrlException e) {
        log.warn("Invalid URL: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ResponseDto.error(e.getMessage()));
    }
    @ExceptionHandler(SpecParsingException.class)
    public ResponseEntity<ResponseDto> handleSpecParsing(SpecParsingException e) {
        log.warn("Spec parsing failed for {}: {}", e.getSpecUrl(), e.getMessage());
        return ResponseEntity.badRequest()
                .body(ResponseDto.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto> handleValidation(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ResponseDto.error(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleException(Exception e) {
        log.error("Exception: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(ResponseDto.error("An unexpected error occurred"));
    }
}
