package com.Application.Gestion.des.PFE.token;

import com.Application.Gestion.des.PFE.user.UserEntity;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "Token")
public class Token {
    @Id
    private String Id;
    private String token;
    public boolean revoked;
    public boolean expired;
    @DBRef
    public UserEntity user;
    @CreatedDate
    private LocalDateTime createdDate;
}
