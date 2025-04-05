package com.Application.Gestion.des.PFE.admin;

import com.Application.Gestion.des.PFE.user.UserEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "users")
@SuperBuilder
@NoArgsConstructor
public class Admin extends UserEntity {
}
