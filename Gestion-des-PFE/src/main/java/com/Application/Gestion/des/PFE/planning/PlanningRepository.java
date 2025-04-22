package com.Application.Gestion.des.PFE.planning;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PlanningRepository extends MongoRepository<Planning,String> {
    Optional<Planning> findByAnneeuniversitaire(String anneeuniversitaire);
}
