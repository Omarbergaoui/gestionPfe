package com.Application.Gestion.des.PFE.salle;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SalleRepository extends MongoRepository<Salle,String> {
    Optional<Salle> findByNom(String name);
    List<Salle> findByDisponibiliteNotContaining(LocalDateTime date);
    List<Salle> findByDisponibiliteContaining(LocalDateTime date);
}
