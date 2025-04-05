package com.Application.Gestion.des.PFE.chefdepartement;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ChefDepartementRepository extends MongoRepository<ChefDepartement,String> {
    Optional<ChefDepartement> findByDepartementId(String ChefDepartementId);
}
