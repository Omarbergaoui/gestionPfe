package com.Application.Gestion.des.PFE.Authentication;


import com.Application.Gestion.des.PFE.Dtos.UserDto;
import com.Application.Gestion.des.PFE.Dtos.EnseignantDto;
import com.Application.Gestion.des.PFE.chefdepartement.ChefDepartement;
import com.Application.Gestion.des.PFE.departement.Departement;
import com.Application.Gestion.des.PFE.email.EmailService;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import com.Application.Gestion.des.PFE.enumeration.Role;
import com.Application.Gestion.des.PFE.token.Token;
import com.Application.Gestion.des.PFE.token.TokenRepository;
import com.Application.Gestion.des.PFE.user.UserEntity;
import com.Application.Gestion.des.PFE.security.JwtService;
import com.Application.Gestion.des.PFE.user.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.SignatureException;
import java.time.LocalDateTime;
import java.util.List;
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


    public Authresponse authenticate(Authrequest request, HttpServletResponse response) {
        var user = userRepository.findByEmail(request.email());
        if (user == null) {
            throw new UserNotFoundException("Utilisateur non trouvé avec l'email: " + request.email());
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
            var jwtToken = jwtService.generateToken(user);
            var refreshToken = jwtService.generateRefreshToken(user);
            revokeAllUserTokens(user);
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
                    .message("Authentication successful.")
                    .build();
        } catch (DisabledException e) {
            throw new RuntimeException("Votre compte est désactivé. Veuillez l'activer pour vous connecter.", e);
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Erreur d'authentification : Mauvais identifiants.", e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur d'authentification : " + e.getMessage(), e);
        }
    }

    public UserDto getUserInfoFromRequestToken(HttpServletRequest request) {
        final String token = extractAccessTokenFromCookies(request);
        if (token == null) {
            throw new BadCredentialsException("Access token cookie manquant.");
        }

        final String userEmail;
        final UserDetails userDetails;

        try {
            userEmail = jwtService.extractUsername(token);
            if (userEmail == null) {
                throw new BadCredentialsException("Impossible d'extraire l'email du token.");
            }

            userDetails = this.userRepository.findByEmail(userEmail);

            if (!jwtService.isTokenValid(token, userDetails)) {
                throw new BadCredentialsException("Token invalide ou expiré.");
            }

        } catch (ExpiredJwtException ex) {
            throw new BadCredentialsException("Token expiré.", ex);
        } catch (UsernameNotFoundException ex) {
            throw new UsernameNotFoundException("Utilisateur non trouvé pour l'email dans le token: " + ex.getMessage(), ex);
        } catch (MalformedJwtException ex) {
            throw new BadCredentialsException("Token invalide ou malformé.", ex);
        } catch (Exception ex) {
            throw new BadCredentialsException("Erreur lors de la validation du token.", ex);
        }

        UserEntity currentUser = (UserEntity) userDetails;

        Role userRole = currentUser.getRole();

        UserDto.UserDtoBuilder<?, ?> builder = UserDto.builder()
                .id(currentUser.getId())
                .firstName(currentUser.getFirstname())
                .lastName(currentUser.getLastname())
                .email(currentUser.getEmail())
                .role(userRole)
                .enabled(currentUser.isEnabled())
                .accountLocked(currentUser.isAccountNonLocked());

        if (userRole == Role.ENSEIGNANT || userRole == Role.CHEFDEPARTEMENT) {
            Enseignant enseignant = (Enseignant) currentUser;

            EnseignantDto.EnseignantDtoBuilder<?, ?> enseignantBuilder = EnseignantDto.builder()
                    .id(enseignant.getId())
                    .firstName(enseignant.getFirstname())
                    .lastName(enseignant.getLastname())
                    .email(enseignant.getEmail())
                    .role(userRole)
                    .enabled(enseignant.isEnabled())
                    .accountLocked(enseignant.isAccountNonLocked());

            enseignantBuilder.matiere(enseignant.getMatiere());
            enseignantBuilder.disponibilite(enseignant.getDisponibilite());
            Departement departement = enseignant.getDepartementId();
            enseignantBuilder.departementId(departement.getId());


            return enseignantBuilder.build();
        }
        return builder.build();

    }

    private void saveUserToken(UserEntity user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .expired(false)
                .revoked(false)
                .build();
        var savedToken = tokenRepository.save(token);
        user.getTokens().add(savedToken);
        userRepository.save(user);
    }

    private void revokeAllUserTokens(UserEntity user) {
        List<Token> validUserTokens = tokenRepository.findByUser(user);
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    private String generateRandomCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String SendMailVerification(EmailVerficationReq req) {
        var user = userRepository.findByEmail(req.email());
        if (user == null) {
            throw new UserNotFoundException("Utilisateur non trouvé avec l'email: " + req.email());
        }
        if (user.getCode() != null && user.getCodeexpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code déja envoyé,please check your box");
        }
        String Code = generateRandomCode();
        user.setCode(Code);
        user.setCodeexpiryDate(LocalDateTime.now().plusHours(3));
        userRepository.save(user);
        emailService.sendEmail(req.email(), Code);
        return "Lien Envoyée Avec Succées,check your box";
    }

    public String VerificationToken(String code) {
        var user = userRepository.findByCode(code);
        if (user == null) {
            throw new UserNotFoundException("Utilisateur non trouvé");
        }
        if (user.getCodeexpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Lien expiré");
        }
        return user.getFirstname() + user.getLastname();
    }

    public String ChangePassword(String code, PasswordReset passwordreq) {
        var user = userRepository.findByCode(code);
        if (user == null) {
            throw new UserNotFoundException("Utilisateur non trouvé");
        }
        if (user.getCodeexpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Lien expiré");
        }
        if (passwordreq.NewPassword().equals(passwordreq.ConfirmPassword())) {
            user.setPassword(passwordEncoder.encode(passwordreq.NewPassword()));
            return "mot de passe changé avec succées";
        } else {
            throw new RuntimeException("les deux mots de passe ne se correspondent pas");
        }
    }

    public Authresponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String refreshToken = extractRefreshTokenFromCookies(request);
        if (refreshToken == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                response.getWriter().write("Missing Refresh Token");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid refresh token");
            return null;
        }
        var user = userRepository.findByEmail(userEmail);
        if (user == null) {
            throw new UserNotFoundException("Utilisateur non trouvé avec l'email: " + userEmail);
        }
        boolean isTokenValid = tokenRepository.findByToken(refreshToken)
                .map(t -> !t.isExpired() && !t.isRevoked())
                .orElse(false);
        System.out.println(isTokenValid);

        if (!jwtService.isTokenValid(refreshToken, user) || !isTokenValid) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired refresh token");

            return null;
        }

        var newAccessToken = jwtService.generateToken(user);
        var newRefreshToken = jwtService.generateRefreshToken(user);
        revokeAllUserTokens(user);
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

    public String extractAccessTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String activateAccount(String code) {
        UserEntity user = userRepository.findByActivationcode(code);
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé ou code invalide.");
        }
        user.setEnable(true);
        user.setActivationcode(null);
        userRepository.save(user);
        return "Compte activé avec succès !";
    }

    public Object getUserByAuthentication(UserEntity user) {
        if (user == null) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        if (user.getRole().equals("CHEFDEPARTEMENT")) {
            return (ChefDepartement) user;
        } else if (user.getRole().equals("ENSEIGNANT")) {
            Enseignant e = (Enseignant) user;
            return EnseignantDto.builder()
                    .id(e.getId())
                    .firstName(e.getFirstname())
                    .lastName(e.getLastname())
                    .role(e.getRole())
                    .disponibilite(e.getDisponibilite())
                    .email(user.getEmail())
                    .departementId(e.getDepartementId())
                    .matiere(e.getMatiere())
                    .build();
        } else {
            return UserDto.builder()
                    .id(user.getId())
                    .firstName(user.getFirstname())
                    .lastName(user.getLastname())
                    .role(user.getRole())
                    .email(user.getEmail())
                    .build();
        }
    }
}

