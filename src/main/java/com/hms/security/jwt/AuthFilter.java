package com.hms.security.jwt;

import com.hms.security.UserDetailServiceImpl;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "app.security.auth-mode",
    havingValue = "local-jwt",
    matchIfMissing = true)
public class AuthFilter extends OncePerRequestFilter {
  private final JWTutils jwtutils;
  private final UserDetailServiceImpl users;
  private final AuthEntryPoint entryPoint;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null) {
      try {
        if (!header.regionMatches(true, 0, "Bearer ", 0, 7) || header.length() <= 7)
          throw new BadCredentialsException("Invalid bearer token");
        var claims = jwtutils.parseToken(header.substring(7));
        var user = users.loadById(Long.valueOf(claims.get("userId", String.class)));
        if (!user.getUsername().equals(claims.getSubject()))
          throw new BadCredentialsException("Account mismatch");
        var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
      } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
        SecurityContextHolder.clearContext();
        entryPoint.commence(request, response, new BadCredentialsException("Invalid token"));
        return;
      }
    }
    chain.doFilter(request, response);
  }
}
