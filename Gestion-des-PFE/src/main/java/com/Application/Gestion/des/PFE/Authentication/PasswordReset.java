package com.Application.Gestion.des.PFE.Authentication;

public record PasswordReset(
        String ConfirmPassword,
        String NewPassword
) {
}
