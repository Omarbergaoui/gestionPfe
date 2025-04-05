package com.Application.Gestion.des.PFE.chefdepartement;

import com.Application.Gestion.des.PFE.departement.Departement;
import com.Application.Gestion.des.PFE.enseignant.Enseignant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Document(collection = "users")
public class ChefDepartement extends Enseignant {
   @DBRef
   private Departement ChefDepartementId;
}
