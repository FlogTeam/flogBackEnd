package golf.flogbackend.domain.member.service;

import golf.flogbackend.domain.member.dto.LoginRequestDto;
import golf.flogbackend.domain.member.dto.SignupRequestDto;
import golf.flogbackend.domain.member.entity.Member;
import golf.flogbackend.domain.member.repository.MemberRepository;
import golf.flogbackend.mail.EmailSenderImpl;
import golf.flogbackend.redis.RedisUtil;
import golf.flogbackend.security.jwt.JwtUtil;
import golf.flogbackend.security.jwt.TokenDto;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final EmailSenderImpl emailSender;

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    @Transactional
    public ResponseEntity<Object> signup(SignupRequestDto signupRequestDto) {
        if (memberRepository.existsById(signupRequestDto.getEmail())) throw new EntityExistsException("EMAIL_ALREADY_EXISTS");
        memberRepository.save(new Member(signupRequestDto.getEmail(), signupRequestDto.getPassword()));

        return new ResponseEntity<>(null, HttpStatus.OK);
    }


}
