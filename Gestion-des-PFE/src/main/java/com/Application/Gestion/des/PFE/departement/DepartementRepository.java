package com.Application.Gestion.des.PFE.departement;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface DepartementRepository extends MongoRepository<Departement,String>{
    Optional<Departement> findByNom(String nom);
}
