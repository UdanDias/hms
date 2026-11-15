package com.hms.security.jwt;

import com.hms.security.HmsUserDetails;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
    name = "app.security.auth-mode",
    havingValue = "local-jwt",
    matchIfMissing = true)
public class JWTutils {
  private final SecretKey key;
  private final String issuer;
  private final String audience;
  private final long ttl;

  public JWTutils(
      @Value("${app.security.jwt.secret}") String secret,
      @Value("${app.security.jwt.issuer:hms}") String issuer,
      @Value("${app.security.jwt.audience:hms-api}") String audience,
      @Value("${app.security.jwt.ttl-seconds:900}") long ttl) {
    byte[] bytes = Decoders.BASE64.decode(secret);
    if (bytes.length < 32)
      throw new IllegalArgumentException(
          "JWT secret must contain at least 32 random bytes, Base64 encoded");
    if (ttl < 60 || ttl > 3600)
      throw new IllegalArgumentException("JWT TTL must be 60 to 3600 seconds");
    key = Keys.hmacShaKeyFor(bytes);
    this.issuer = issuer;
    this.audience = audience;
    this.ttl = ttl;
  }

  public long getExpiresIn() {
    return ttl;
  }

  public String generateToken(HmsUserDetails user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.getUsername())
        .issuer(issuer)
        .audience()
        .add(audience)
        .and()
        .claim("userId", user.userId().toString())
        .claim("token_use", "access")
        .id(UUID.randomUUID().toString())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(ttl)))
        .signWith(key, Jwts.SIG.HS256)
        .compact();
  }

  public Claims parseToken(String token) {
    var signed =
        Jwts.parser()
            .verifyWith(key)
            .requireIssuer(issuer)
            .requireAudience(audience)
            .require("token_use", "access")
            .build()
            .parseSignedClaims(token);
    Claims claims = signed.getPayload();
    if (!"HS256".equals(signed.getHeader().getAlgorithm())
        || claims.getExpiration() == null
        || claims.getIssuedAt() == null
        || claims.getSubject() == null
        || claims.getSubject().isBlank()
        || claims.getId() == null
        || claims.getIssuedAt().after(new Date(System.currentTimeMillis() + 30000)))
      throw new JwtException("Invalid access token");
    return claims;
  }

  public boolean validateToken(String token) {
    try {
      parseToken(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public String getUsernameFromToken(String token) {
    return parseToken(token).getSubject();
  }
}
