package com.Application.Gestion.des.PFE.security;

import com.Application.Gestion.des.PFE.Authentication.UserNotFoundException;
import com.Application.Gestion.des.PFE.token.Token;
import com.Application.Gestion.des.PFE.token.TokenRepository;
import com.Application.Gestion.des.PFE.user.UserRepository;
import com.Application.Gestion.des.PFE.user.UserEntity;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;


    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String refreshToken = extractCookieValue(request, "refresh_token");
        if (refreshToken == null) {
            clearAuthCookies(response);
            SecurityContextHolder.clearContext();
            return;
        }

        String userEmail;
        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("Refresh token has expired");
        } catch (SignatureException | MalformedJwtException e) {
            throw new InvalidTokenException("Invalid refresh token");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during token parsing", e);
        }

        UserEntity user = userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));
        if (user != null) {
            tokenRepository.findByToken(refreshToken).ifPresent(token -> {
                if (!token.isRevoked() && !token.isExpired()) {
                    token.setRevoked(true);
                    token.setExpiryDate(LocalDateTime.now());
                    tokenRepository.save(token);
                }
            });
        }

        clearAuthCookies(response);
        SecurityContextHolder.clearContext();
    }


    private void clearAuthCookies(HttpServletResponse response) {

        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refresh_token", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }
}