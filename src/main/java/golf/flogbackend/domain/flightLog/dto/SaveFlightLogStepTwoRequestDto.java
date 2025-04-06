package golf.flogbackend.domain.flightLog.dto;

import golf.flogbackend.domain.member.dto.LoginRequestDto;
import golf.flogbackend.domain.member.entity.Member;
import golf.flogbackend.security.jwt.JwtUtil;
import golf.flogbackend.security.jwt.TokenDto;
import golf.flogbackend.security.userDetails.UserDetailsImpl;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.UUID;

public class SaveFlightLogStepTwoRequestDto {
}
