package com.boot.security.provider;

import com.boot.security.role.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long validityInMilliseconds;

    public JwtTokenProvider(
            @Value("${jwt.secret:vmfhaltksdlqlshstnwhsghkdlxldmsep01234567890123456789}") String secretKey,
            @Value("${jwt.expiration:3600000}") long validityInMilliseconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.validityInMilliseconds = validityInMilliseconds;
    }

    // 🌟 [핵심 수정] 토큰 생성 시 Subject에 아이디를 직접 박아넣습니다.
    public String createToken(String loginId, UserRole role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(loginId) // 🚩 여기가 비어있어서 null이 떴던 것입니다.
                .claim("role", role.name())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key)
                .compact();
    }

    // 🌟 [핵심 수정] 토큰을 읽을 때 Subject를 정확히 꺼내옵니다.
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String loginId = claims.getSubject(); // 🚩 생성할 때 넣은 아이디를 가져옵니다.
        String role = claims.get("role", String.class);

        // 정식 UserDetails 객체 생성 (username에 loginId를 넣음)
        UserDetails userDetails = User.builder()
                .username(loginId != null ? loginId : "unknown")
                .password("")
                .roles(role != null ? role.replace("ROLE_", "") : "USER")
                .build();

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}