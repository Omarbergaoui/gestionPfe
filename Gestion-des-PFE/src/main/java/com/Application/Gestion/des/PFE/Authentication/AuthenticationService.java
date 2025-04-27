package com.Application.Gestion.des.PFE.Authentication;

import com.Application.Gestion.des.PFE.Dtos.ChefEnseignantDto;
import com.Application.Gestion.des.PFE.Dtos.UserDto;
import com.Application.Gestion.des.PFE.Dtos.EnseignantDto;
import com.Application.Gestion.des.PFE.email.EmailService;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.enseignant.EnseignantRepository;
import com.Application.Gestion.des.PFE.enumeration.Role;
import com.Application.Gestion.des.PFE.token.Token;
import com.Application.Gestion.des.PFE.token.TokenRepository;
import com.Application.Gestion.des.PFE.user.UserEntity;
import com.Application.Gestion.des.PFE.security.JwtService;
import com.Application.Gestion.des.PFE.user.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final EnseignantRepository enseignantRepository;

    public Authresponse authenticate(Authrequest request, HttpServletResponse response) {
        var user = userRepository.findByEmail(request.email()).orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.email()));
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            saveUserToken(user, refreshToken);

            Cookie accessTokenCookie = new Cookie("access_token", jwtToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(true);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(60 * 60 * 24);
            response.addCookie(accessTokenCookie);

            Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7);
            response.addCookie(refreshTokenCookie);

            return Authresponse.builder()
                    .message("You have successfully logged in. Welcome back!")
                    .build();

        } catch (DisabledException e) {
            throw new RuntimeException("Your account is disabled. Please activate it to log in.", e);
        } catch (LockedException e) {
            throw new RuntimeException("Your account is locked. Please contact the administrator.", e);
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Authentication failed: Invalid credentials.", e);
        } catch (Exception e) {
            throw new RuntimeException("Authentication error: " + e.getMessage(), e);
        }
    }

    private void saveUserToken(UserEntity user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .expiryDate(LocalDateTime.now().plusDays(30))
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private String generateRandomCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String sendMailVerification(EmailVerficationReq req) {
        var user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new UserNotFoundException("No user found with email: " + req.email()));

        if (user.getCode() != null && user.getCodeexpiryDate().isAfter(LocalDateTime.now())) {
            throw new VerificationCodeAlreadySentException("A verification code has already been sent. Please check your email.");
        }
        String code = generateRandomCode();
        user.setCode(code);
        user.setCodeexpiryDate(LocalDateTime.now().plusHours(3));
        userRepository.save(user);
        emailService.sendEmail(req.email(), code);
        return "Verification link sent successfully. Please check your email.";
    }


    public String verifyToken(String code) {
        var user = userRepository.findByCode(code);
        if (user == null) {
            throw new UserNotFoundException("User not found for the provided verification code.");
        }

        if (user.getCodeexpiryDate().isBefore(LocalDateTime.now())) {
            throw new VerificationLinkExpiredException("The verification link has expired.");
        }

        return user.getFirstname() + " " + user.getLastname();
    }


    public String changePassword(String code, PasswordReset passwordReq) {
        var user = userRepository.findByCode(code);
        if (user == null) {
            throw new UserNotFoundException("No user found for the provided verification code.");
        }

        if (user.getCodeexpiryDate().isBefore(LocalDateTime.now())) {
            throw new VerificationLinkExpiredException("The password reset link has expired.");
        }

        if (!passwordReq.NewPassword().equals(passwordReq.ConfirmPassword())) {
            throw new PasswordMismatchException("The new password and confirmation do not match.");
        }
        user.setPassword(passwordEncoder.encode(passwordReq.NewPassword()));
        user.setCode(null);
        user.setCodeexpiryDate(null);
        userRepository.save(user);
        return "Password changed successfully.";
    }


    public Authresponse refreshToken(HttpServletRequest request, HttpServletResponse response, UserEntity user) throws IOException {
        final String refreshToken = extractRefreshTokenFromCookies(request);
        if (refreshToken == null || refreshToken.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing Refresh Token");
            return null;
        }
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User Not Authorized");
            return null;
        }
        final String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid refresh token");
            return null;
        }
        if (!user.getEmail().equals(userEmail)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User Not Authorized");
            return null;
        }
        boolean isTokenValid = tokenRepository.findByToken(refreshToken)
                .map(t -> !t.isExpired() && !t.isRevoked())
                .orElse(false);

        if (!jwtService.isTokenValid(refreshToken, user) || !isTokenValid) {
            Cookie refreshTokenCookie = new Cookie("refresh_token", null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired refresh token");
            return Authresponse.builder()
                    .message("Invalid or expired refresh token.")
                    .build();

        }
        if (tokenRepository.findByToken(refreshToken).isPresent()) {
            Token token = tokenRepository.findByToken(refreshToken).get();
            token.setExpiryDate(LocalDateTime.now());
            token.setRevoked(true);
            tokenRepository.save(token);
        }
        var newAccessToken = jwtService.generateToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);
        saveUserToken(user, newRefreshToken);

        Cookie accessTokenCookie = new Cookie("access_token", newAccessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 60 * 24);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refresh_token", newRefreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7);
        response.addCookie(refreshTokenCookie);
        return Authresponse.builder()
                .message("Token refreshed successfully.")
                .build();
    }

    public String extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refresh_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String activateAccount(String code) {
        UserEntity user = userRepository.findByActivationcode(code);
        if (user == null) {
            throw new ActivationCodeInvalidException("User not found or activation code is invalid.");
        }
        user.setEnable(true);
        user.setActivationcode(null);
        userRepository.save(user);
        return "Account activated successfully!";
    }


    public Object getUserByAuthentication(UserEntity user) {
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        if (user.getRole().equals(Role.ADMIN.name())) {
            return UserDto.builder()
                    .id(user.getId())
                    .firstName(user.getFirstname())
                    .lastName(user.getLastname())
                    .role(user.getRole())
                    .email(user.getEmail())
                    .build();
        }
        Enseignant ens = enseignantRepository.findById(user.getId()).get();
        return EnseignantDto.builder()
                .id(ens.getId())
                .firstName(ens.getFirstname())
                .lastName(ens.getLastname())
                .role(ens.getRole())
                .email(ens.getEmail())
                .matiere(ens.getMatiere())
                .build();

    }

}

