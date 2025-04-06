package golf.flogbackend.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@RequiredArgsConstructor
public class EmailSenderImpl implements EmailSender {

    private final SpringTemplateEngine templateEngine;
    private final MailProperties properties;
    private final JavaMailSender javaMailSender;

    @Override
    public void send(String email, String tempPassword) throws MessagingException {

        Context context = new Context();
        context.setVariable("tempPassword", tempPassword);
        String html = templateEngine.process("index.html", context);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(properties.getUsername());
        helper.setTo(email);
        helper.setSubject("Flog 임시 비밀번호");
        helper.setText(html, true);

        javaMailSender.send(message);
    }
}