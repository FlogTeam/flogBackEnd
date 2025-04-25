package golf.flogbackend.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.security.InvalidKeyException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import java.security.InvalidParameterException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Message> handleException(Exception e) {
        e.printStackTrace();
        log.error("===== Exception 메세지 확인 : {}", e.getMessage());
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<Message> handleRuntimeException(RuntimeException e) {
        e.printStackTrace();
        log.error("===== Exception 메세지 확인 : {}", e.getMessage());
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = JsonProcessingException.class)
    public ResponseEntity<Message> handleJsonProcessingException(JsonProcessingException e) {
        e.printStackTrace();
        log.error("===== Exception 메세지 확인 : {}", e.getMessage());
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = RestClientException.class)
    public ResponseEntity<Message> handleRestClientException(RestClientException e) {
        e.printStackTrace();
        log.error("===== Exception 메세지 확인 : {}", e.getMessage());
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    public ResponseEntity<Message> handleHttpClientErrorException(HttpClientErrorException e) {
        e.printStackTrace();
        log.error("===== Exception 메세지 확인 : {}", e.getMessage());
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.BAD_GATEWAY);
    }

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<Message> handleEntityNotFoundException(EntityNotFoundException e) {
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = EntityExistsException.class)
    public ResponseEntity<Message> handleEntityExistsException(EntityExistsException e) {
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = UsernameNotFoundException.class)
    public ResponseEntity<Message> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = InvalidParameterException.class)
    public ResponseEntity<Message> handleInvalidParameterException(InvalidParameterException e) {
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public ResponseEntity<Message> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = IllegalStateException.class)
    public ResponseEntity<Message> handleIllegalStateException(IllegalStateException e) {
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = InvalidKeyException.class)
    public ResponseEntity<Message> handleInvalidKeyException(InvalidKeyException e) {
        return new ResponseEntity<>(Message.buildMessage(e), HttpStatus.BAD_REQUEST);
    }
}
