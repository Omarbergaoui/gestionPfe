package com.Application.Gestion.des.PFE.salle;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SalleRepository extends MongoRepository<Salle,String> {
    Optional<Salle> findByNom(String name);
}
