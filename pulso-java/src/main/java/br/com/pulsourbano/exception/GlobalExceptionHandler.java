package br.com.pulsourbano.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> notFound(ResourceNotFoundException e) {
        return ResponseEntity.status(404).body(ErrorResponseDTO.of(404, "Not Found", e.getMessage()));
    }

    @ExceptionHandler(EmailJaExisteException.class)
    public ResponseEntity<ErrorResponseDTO> conflict(EmailJaExisteException e) {
        return ResponseEntity.status(409).body(ErrorResponseDTO.of(409, "Conflict", e.getMessage()));
    }

    @ExceptionHandler(IngestaoException.class)
    public ResponseEntity<ErrorResponseDTO> ingestao(IngestaoException e) {
        log.error("Falha de ingestão: {}", e.getMessage());
        return ResponseEntity.status(503).body(ErrorResponseDTO.of(503, "Service Unavailable",
                "Serviço de dados orbitais indisponível"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> validation(MethodArgumentNotValidException e) {
        List<String> campos = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(
                new ErrorResponseDTO(400, "Bad Request", "Validação falhou", campos, LocalDateTime.now()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> auth(AuthenticationException e) {
        return ResponseEntity.status(401).body(ErrorResponseDTO.of(401, "Unauthorized", "Credenciais inválidas"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> generic(Exception e) {
        log.error("Erro não tratado", e);
        return ResponseEntity.status(500).body(ErrorResponseDTO.of(500, "Internal Server Error",
                "Erro interno do servidor"));
    }
}
