package com.Application.Gestion.des.PFE.Authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@RestController
@RequestMapping("/Authentication")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;
    @PostMapping("/Login")
    public ResponseEntity<Authresponse> authenticate(@RequestBody Authrequest request,HttpServletResponse response) {
        return ResponseEntity.ok(service.authenticate(request,response));
    }

    @PostMapping("/SendMail")
    public ResponseEntity<String> SendMailVerif(@RequestBody EmailVerficationReq req){
        return ResponseEntity.ok(service.SendMailVerification(req));
    }

    @GetMapping("/Verify/{code}")
    public ResponseEntity<String> Verify(@PathVariable String code){
        return ResponseEntity.ok(service.VerificationToken(code));
    }

    @PostMapping("/Change-Password/{code}")
    public ResponseEntity<String> ChangePass(@PathVariable String code,@RequestBody PasswordReset passwordReset){
        return ResponseEntity.ok(service.ChangePassword(code,passwordReset));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Authresponse> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            Authresponse authResponse = service.refreshToken(request, response);
            if (authResponse == null) {
                return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).build();
            }
            return ResponseEntity.ok(authResponse);
        } catch (IOException e) {
            return ResponseEntity.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam String code) {
        String result = service.activateAccount(code);
        return ResponseEntity.ok(result);
    }
}
