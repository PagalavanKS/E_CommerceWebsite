package com.ecommerce.exception;
import jakarta.validation.ConstraintViolationException; import org.springframework.http.*; import org.springframework.security.access.AccessDeniedException; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.time.Instant; import java.util.List;
@RestControllerAdvice
public class GlobalExceptionHandler {
 @ExceptionHandler(ApiException.class) public ResponseEntity<ApiError> handleApi(ApiException ex){ return build(ex.getStatus(), ex.getMessage()); }
 @ExceptionHandler(MethodArgumentNotValidException.class) public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex){ List<String> d=ex.getBindingResult().getFieldErrors().stream().map(e->e.getField()+": "+e.getDefaultMessage()).toList(); return ResponseEntity.badRequest().body(new ApiError(Instant.now(),400,"Bad Request",d)); }
 @ExceptionHandler(ConstraintViolationException.class) public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex){ List<String> d=ex.getConstraintViolations().stream().map(v->v.getPropertyPath()+": "+v.getMessage()).toList(); return ResponseEntity.badRequest().body(new ApiError(Instant.now(),400,"Bad Request",d)); }
 @ExceptionHandler(AccessDeniedException.class) public ResponseEntity<ApiError> handleDenied(AccessDeniedException ex){ return build(HttpStatus.FORBIDDEN,"You do not have permission to perform this action"); }
 @ExceptionHandler(Exception.class) public ResponseEntity<ApiError> handleUnexpected(Exception ex){ return build(HttpStatus.INTERNAL_SERVER_ERROR,"Unexpected server error"); }
 private ResponseEntity<ApiError> build(HttpStatus s,String detail){ return ResponseEntity.status(s).body(new ApiError(Instant.now(),s.value(),s.getReasonPhrase(),List.of(detail))); }
}