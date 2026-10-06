package pablocos.gestor_biblioteca.kinal.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Manejo global de errores. Extiende ResponseEntityExceptionHandler para que las excepciones
 * estandar de Spring MVC (JSON malformado, metodo no soportado, ruta inexistente, etc.)
 * tambien usen el formato ErrorResponse. Nunca se expone el stack trace.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Object> handleBusinessRule(BusinessRuleException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        log.warn("Violacion de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "La operación viola una restricción de datos (valor duplicado o relación existente)", request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "No tiene permisos para acceder a este recurso", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Error no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, detalle.isEmpty() ? "Datos de entrada inválidos" : detalle, request);
    }

    /** Punto comun de las demas excepciones estandar de Spring MVC. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
                                                             HttpHeaders headers,
                                                             HttpStatusCode statusCode,
                                                             WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String mensaje = status != null ? status.getReasonPhrase() : "Error";
        return build(statusCode, mensaje, request);
    }

    private ResponseEntity<Object> build(HttpStatusCode statusCode, String message, WebRequest request) {
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String error = status != null ? status.getReasonPhrase() : "Error";
        ErrorResponse body = new ErrorResponse(Instant.now(), statusCode.value(), error, message, path(request));
        return ResponseEntity.status(statusCode).body(body);
    }

    private String path(WebRequest request) {
        return request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI()
                : "";
    }
}