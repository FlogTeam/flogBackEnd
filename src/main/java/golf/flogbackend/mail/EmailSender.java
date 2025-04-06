package golf.flogbackend.mail;


import jakarta.mail.MessagingException;

public interface EmailSender {
    void send(String email, String tempPassword) throws MessagingException;
}