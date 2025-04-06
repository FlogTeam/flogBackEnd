package golf.flogbackend.security.jwt;

import golf.flogbackend.redis.RedisUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import golf.flogbackend.domain.member.entity.Member;
import golf.flogbackend.security.userDetails.UserDetailsServiceImpl;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.Key;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private static final String BEARER_PREFIX = "Bearer ";
    public static final String ACCESS_KEY = "ACCESS-TOKEN";
    public static final String REFRESH_KEY = "REFRESH-TOKEN";
    private static final long ACCESS_TIME = Duration.ofMinutes(30).toMillis();
    private static final long REFRESH_TIME = Duration.ofDays(7).toMillis();

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;

    private final UserDetailsServiceImpl userDetailsService;
    private final RedisUtil redisUtil;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String resolveToken(HttpServletRequest request, String token) {
        String tokenName = token.equals("ACCESS-TOKEN") ? ACCESS_KEY : REFRESH_KEY;
        String bearerToken = request.getHeader(tokenName);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public TokenDto createAllToken(Member members) {
        return new TokenDto(createToken(members, "Access"), createToken(members, "Refresh"));
    }

    public String createToken(Member members, String type) {
        Date date = new Date();
        if(type.equals("Access")) {
            return BEARER_PREFIX
                    + Jwts.builder()
                    .setSubject(members.getEmail())
                    .claim("email", members.getEmail())
                    .signWith(key)
                    .setIssuedAt(date)
                    .setExpiration(new Date(date.getTime() + ACCESS_TIME))
                    .compact();
        } else {
            return BEARER_PREFIX
                    + Jwts.builder()
                    .setSubject(members.getEmail())
                    .signWith(key)
                    .setIssuedAt(date)
                    .setExpiration(new Date(date.getTime() + REFRESH_TIME))
                    .compact();
        }
    }

    public String createNewRefreshToken(String email, long time) {
        Date date = new Date();
        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(email)
                        .setExpiration(new Date(date.getTime() + time))
                        .setIssuedAt(date)
                        .signWith(key)
                        .compact();
    }

    public boolean validateToken(String token) {
        if (redisUtil.hasKeyBlackList(token)) {
            return false;
        }
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, 유효하지 않은 JWT 서명 입니다.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, 만료된 JWT token 입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
        }
        return false;
    }

    public String getMemberInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public Authentication createAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public boolean existsRefreshToken(String email) {
        return redisUtil.get(email) != null;
    }

    public void setHeaderToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setHeader(ACCESS_KEY, accessToken);
        response.setHeader(REFRESH_KEY, refreshToken);
    }

    public long getExpirationTime(String token) {
        Date expirationDate = Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(token).getBody().getExpiration();
        Date now = new Date();
        return expirationDate.getTime() - now.getTime();
    }
}