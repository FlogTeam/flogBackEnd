package golf.flogbackend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Message {
    private String exception;
    private String message;
    private LocalDateTime timestamp;
}
