package golf.flogbackend.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class Message {
    private int code;
    private String exception;
    private String message;
    private LocalDateTime timestamp;

    public static Message buildMessage(Exception e) {
        String message = e.getMessage();
        if (message.startsWith("code : ")) {
            if (message.charAt(8) -'0' >= 0 && message.charAt(8) -'0' >= 0) {
                return Message.builder()
                        .code(Integer.parseInt(message.substring(7, 9)))
                        .exception(e.getClass().getSimpleName())
                        .message(message.substring(9))
                        .timestamp(LocalDateTime.now())
                        .build();
            }
            return Message.builder()
                    .code(message.charAt(7) - '0')
                    .exception(e.getClass().getSimpleName())
                    .message(message.substring(8))
                    .timestamp(LocalDateTime.now())
                    .build();
        } else {
            return Message.builder()
                    .code(0)
                    .exception(e.getClass().getSimpleName())
                    .message(e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }
}
