package Chess.demo.Exceptions;

import Chess.demo.ModelsandDTO.APIResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GeneralExceptionHandler {

    @ExceptionHandler(InvalidChessException.class)
    public ResponseEntity<APIResponse<?>> handleInvalidChessPosition(
            InvalidChessException ex,
            HttpServletRequest request
    ) {
        APIResponse<?> error = new APIResponse<>(
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                null
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        APIResponse<Map<String,String>> errorResponse = new APIResponse<>(
                "Validation Failed",
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                errors
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
