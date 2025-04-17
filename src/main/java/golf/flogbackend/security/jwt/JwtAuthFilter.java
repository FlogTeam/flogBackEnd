package golf.flogbackend.security.jwt;

import golf.flogbackend.domain.member.entity.Member;
import golf.flogbackend.domain.member.repository.MemberRepository;
import golf.flogbackend.redis.RedisUtil;
import io.jsonwebtoken.security.InvalidKeyException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static golf.flogbackend.security.jwt.JwtUtil.ACCESS_KEY;
import static golf.flogbackend.security.jwt.JwtUtil.REFRESH_KEY;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final MemberRepository membersRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String access_token = jwtUtil.resolveToken(request, ACCESS_KEY);
        String refresh_token = jwtUtil.resolveToken(request, REFRESH_KEY);

        if (access_token != null) {
            if (jwtUtil.validateToken(access_token)) {
                setAuthentication(jwtUtil.getMemberInfoFromToken(access_token));
            } else if (refresh_token != null && jwtUtil.existsRefreshToken(jwtUtil.getMemberInfoFromToken(refresh_token))) {
                String email = jwtUtil.getMemberInfoFromToken(refresh_token);

                Member member = membersRepository.findById(email).orElseThrow(
                        () -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다. 이메일 : " + email));

                String newAccessToken = jwtUtil.createToken(member, "Access");

                long refreshTokenTime = jwtUtil.getExpirationTime(refresh_token);
                String newRefreshToken = jwtUtil.createNewRefreshToken(email, refreshTokenTime);

                redisUtil.set(email, newRefreshToken, refreshTokenTime);
                jwtUtil.setHeaderToken(response, newAccessToken, newRefreshToken);
            } else {
                throw new InvalidKeyException("인증 실패");
            }
        }
        filterChain.doFilter(request, response);
    }

    public void setAuthentication(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = jwtUtil.createAuthentication(email);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }
}