package golf.flogbackend.domain.member.controller;

import golf.flogbackend.domain.member.dto.LoginRequestDto;
import golf.flogbackend.domain.member.dto.SignupRequestDto;
import golf.flogbackend.domain.member.dto.UpdateNickNameRequestDto;
import golf.flogbackend.domain.member.service.MemberService;
import golf.flogbackend.security.userDetails.UserDetailsImpl;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;

    @GetMapping("/check-mail")
    public ResponseEntity<String> checkMail(@RequestParam("mail") String mail) {
        return memberService.checkEmail(mail);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequestDto signupRequestDto) {
        return memberService.signup(signupRequestDto);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto loginRequestDto, HttpServletResponse httpServletResponse) {
        return memberService.login(loginRequestDto, httpServletResponse);
    }

    @GetMapping("/mail")
    public ResponseEntity<String> mailSend(@AuthenticationPrincipal UserDetailsImpl userPrincipal) throws MessagingException {
        return memberService.mailSend(userPrincipal.getMember());
    }

    @PutMapping("/nickname")
    public ResponseEntity<String> updateNickname(@AuthenticationPrincipal UserDetailsImpl userPrincipal, @RequestBody UpdateNickNameRequestDto requestDto) {
        return memberService.updateNickname(userPrincipal.getMember(), requestDto);
    }
}
