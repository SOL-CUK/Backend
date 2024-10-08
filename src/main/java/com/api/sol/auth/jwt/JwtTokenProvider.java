package com.api.sol.auth.jwt;
import com.api.sol.auth.entity.TokenType;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    public static final String TOKEN_TYPE_CLAIM_NAME = "tokenType";
    public static final long ACCESS_TIME = Duration.ofHours(1).toMillis(); // 만료시간 1시간

    public String generateAccessToken(String clientId) {
        return buildToken(TokenType.ACCESS, clientId, ACCESS_TIME);
    }

    private String buildToken(TokenType tokenType, String clientId, Long expiration) {
        return Jwts.builder()
                .setSubject(clientId)
                .claim(TOKEN_TYPE_CLAIM_NAME, tokenType.toString())
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(Base64.getEncoder().encodeToString(secretKey.getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            log.error("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    public String extractClientId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
