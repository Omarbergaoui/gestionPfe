package com.Application.Gestion.des.PFE.config;

import com.Application.Gestion.des.PFE.Authentication.*;
import com.Application.Gestion.des.PFE.disponibilte.AvailableDateException;
import com.Application.Gestion.des.PFE.disponibilte.DisponibilityNotFoundException;
import com.Application.Gestion.des.PFE.disponibilte.InvalidDateException;
import com.Application.Gestion.des.PFE.enseignant.EnseignantAlreadyExistsException;
import com.Application.Gestion.des.PFE.salle.*;
import com.Application.Gestion.des.PFE.security.ExpiredTokenException;
import com.Application.Gestion.des.PFE.security.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.Application.Gestion.des.PFE.departement.DepartmentNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(SalleNotFoundException.class)
    public ResponseEntity<String> handleSalleNotFoundException(SalleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(SalleException.class)
    public ResponseEntity<String> handleSalleException(SalleException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }


    @ExceptionHandler(VerificationCodeAlreadySentException.class)
    public ResponseEntity<String> handleVerificationCodeAlreadySent(VerificationCodeAlreadySentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<String> handleExpiredToken(ExpiredTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(VerificationLinkExpiredException.class)
    public ResponseEntity<String> handleVerificationLinkExpired(VerificationLinkExpiredException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(ActivationCodeInvalidException.class)
    public ResponseEntity<String> handleActivationCodeInvalid(ActivationCodeInvalidException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }


    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<String> handlePasswordMismatch(PasswordMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(EnseignantAlreadyExistsException.class)
    public ResponseEntity<String> handleEnseignantAlreadyExists(EnseignantAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<String> handleDepartmentNotFound(DepartmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }


    @ExceptionHandler(DisponibilityNotFoundException.class)
    public ResponseEntity<String> handleDisponibilityNotFoundException(DisponibilityNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidDateException.class)
    public ResponseEntity<String> handleInvalidDateException(InvalidDateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(AvailableDateException.class)
    public ResponseEntity<String> handleAvailableDateException(AvailableDateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}