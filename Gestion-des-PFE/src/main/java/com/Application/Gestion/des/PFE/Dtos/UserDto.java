package com.Application.Gestion.des.PFE.Dtos; // Adjust package

import com.Application.Gestion.des.PFE.enumeration.Role; // Import Role enum
import com.Application.Gestion.des.PFE.token.Token;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder; // Use SuperBuilder if extending

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private List<Token> tokens = new ArrayList<>();
    private boolean enabled;
    private boolean accountLocked;

}