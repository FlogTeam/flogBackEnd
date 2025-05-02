package golf.flogbackend.domain.member.service;

import golf.flogbackend.domain.member.dto.LoginRequestDto;
import golf.flogbackend.domain.member.dto.SignupRequestDto;
import golf.flogbackend.domain.member.dto.UpdateNickNameRequestDto;
import golf.flogbackend.domain.member.entity.Member;
import golf.flogbackend.domain.member.repository.MemberRepository;
import golf.flogbackend.domain.nickname.service.NicknameService;
import golf.flogbackend.mail.EmailSenderImpl;
import golf.flogbackend.redis.RedisUtil;
import golf.flogbackend.security.jwt.JwtUtil;
import golf.flogbackend.security.jwt.TokenDto;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final EmailSenderImpl emailSender;

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final NicknameService nicknameService;

    public ResponseEntity<String> checkEmail(String email) {
        if (findMemberByEmail(email)) throw new EntityExistsException("EMAIL_ALREADY_EXISTS : " + email);
        return ResponseEntity.ok("이메일 중복 확인 완료");
    }

    @Transactional
    public ResponseEntity<String> signup(SignupRequestDto signupRequestDto) {
        if (findMemberByEmail(signupRequestDto.getEmail()))
            throw new EntityExistsException("EMAIL_ALREADY_EXISTS : " + signupRequestDto.getEmail());
        memberRepository.save(new Member(signupRequestDto.getEmail(), nicknameService.getRandomNickname(), passwordEncoder.encode(signupRequestDto.getPassword())));

        return ResponseEntity.ok("회원 가입 성공");
    }

    @Transactional(readOnly = true)
    public ResponseEntity<String> login(LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse) {
        String email = loginRequestDto.getEmail();
        Member member = memberRepository.findById(email).orElseThrow(
                () -> new UsernameNotFoundException("사용자를 찾을 수 없습니다. email : " + email)
        );

        if (!passwordEncoder.matches(loginRequestDto.getPassword(), member.getPassword())) {
            throw new InvalidParameterException("비밀번호를 틀렸습니다.");
        }

        TokenDto tokenDto = jwtUtil.createAllToken(member);
        String refreshToken = tokenDto.getRefreshToken();
        redisUtil.set(email, refreshToken, Duration.ofDays(7).toMillis());
        httpServletResponse.addHeader(JwtUtil.ACCESS_KEY, tokenDto.getAccessToken());
        httpServletResponse.addHeader(JwtUtil.REFRESH_KEY, tokenDto.getRefreshToken());
        return ResponseEntity.ok("로그인 성공");
    }

    public ResponseEntity<String> mailSend(Member member) throws MessagingException {
        emailSender.send(member.getEmail(), UUID.randomUUID().toString().substring(0, 8));
        return ResponseEntity.ok("");
    }

    private boolean findMemberByEmail(String email) {
        return memberRepository.existsById(email);
    }

    @Transactional
    public ResponseEntity<String> updateNickname(Member member, UpdateNickNameRequestDto requestDto) {
        String nickname = requestDto.getNickname();
        if (memberRepository.existsByNickname(nickname)) throw new EntityExistsException("NICKNAME_ALREADY_EXISTS : " + nickname);
        member.setNickname(nickname);
        memberRepository.save(member);
        return ResponseEntity.ok(member.getNickname());
    }
}
