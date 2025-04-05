package com.Application.Gestion.des.PFE.Authentication;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterReq {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private String Matiere;
}
