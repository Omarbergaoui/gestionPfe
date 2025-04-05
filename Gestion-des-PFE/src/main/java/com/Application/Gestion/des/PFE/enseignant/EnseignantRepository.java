package com.Application.Gestion.des.PFE.enseignant;

import com.Application.Gestion.des.PFE.departement.Departement;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EnseignantRepository extends MongoRepository<Enseignant,String> {
    Enseignant findByEmail(String email);
    List<Enseignant> findByDepartementId(Departement departement);
}
