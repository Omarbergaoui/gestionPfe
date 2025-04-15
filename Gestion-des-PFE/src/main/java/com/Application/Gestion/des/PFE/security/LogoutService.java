package com.Application.Gestion.des.PFE.security;

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
        log.debug("Logout process started.");

        final String refreshToken = extractCookieValue(request, "refresh_token");

        if (refreshToken == null) {
            log.warn("Logout attempt without refresh token cookie.");
            clearAuthCookies(response);
            SecurityContextHolder.clearContext();
            return;
        }

        String userEmail = null;
        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (ExpiredJwtException e) {
            log.warn("Logout attempt with expired refresh token.");
        } catch (SignatureException | MalformedJwtException e) {
            log.warn("Logout attempt with invalid refresh token: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Error extracting username from refresh token during logout", e);
        }

        if (userEmail != null) {
            UserEntity user = userRepository.findByEmail(userEmail);
            if (user != null) {
                log.info("Invalidating tokens for user: {}", userEmail);

                revokeAllUserTokens(user);
            } else {
                log.warn("User not found for email {} extracted from refresh token during logout.", userEmail);
            }
        }

        log.debug("Clearing authentication cookies.");
        clearAuthCookies(response);

        log.debug("Clearing SecurityContextHolder.");
        SecurityContextHolder.clearContext();

        log.info("Logout process completed for request associated with email: {} (if extracted)", userEmail);
    }

    private void revokeAllUserTokens(UserEntity user) {
        List<Token> validUserTokens = tokenRepository.findByUserAndExpiredFalseAndRevokedFalse(user.getId());
        if (validUserTokens.isEmpty()){
            log.debug("No valid tokens found to revoke for user: {}", user.getEmail());
            return;
        }
        log.debug("Revoking {} valid token(s) for user: {}", validUserTokens.size(), user.getEmail());
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
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