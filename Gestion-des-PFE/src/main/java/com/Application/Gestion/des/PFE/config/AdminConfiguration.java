package com.Application.Gestion.des.PFE.config;

import com.Application.Gestion.des.PFE.admin.AdminRepository;
import com.Application.Gestion.des.PFE.admin.Admin;
import com.Application.Gestion.des.PFE.email.EmailService;
import com.Application.Gestion.des.PFE.enumeration.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class AdminConfiguration {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private String generateRandomCode() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    String Code=generateRandomCode();

    @Bean
    public CommandLineRunner createAdmin() {
        return args -> {
            if (adminRepository.findByRole("ADMIN").isEmpty()) {
                Admin admin = new Admin();
                admin.setFirstname("admin");
                admin.setLastname("admin");
                admin.setPassword(passwordEncoder.encode("securepass"));
                admin.setEmail("admin@example.com");
                admin.setRole(Role.ADMIN);
                admin.setEnable(true);
                admin.setActivationcode(Code);
                admin.setAccountLocked(false);
                emailService.sendActivationEmail("admin@example.com",Code);
                adminRepository.save(admin);
                System.out.println("✅ Un Admin created successfully.");
            } else {
                System.out.println("✅ Un Admin existe déjà.");
            }

        };
    }
}
