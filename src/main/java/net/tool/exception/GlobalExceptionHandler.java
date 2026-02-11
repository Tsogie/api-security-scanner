package net.tool.exception;


import lombok.extern.slf4j.Slf4j;
import net.tool.dto.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler{

    @ExceptionHandler(SpecParsingException.class)
    public ResponseEntity<ResponseDto> handleSpecParsingException(SpecParsingException e) {
        log.warn("Spec parsing failed for {}: {}", e.getSpecUrl(), e.getMessage());
        return ResponseEntity.badRequest()
                .body(ResponseDto.error(e.getMessage()));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleException(Exception e) {
        log.warn("Exception: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(ResponseDto.error("An unexpected error occurred"));
    }
}
