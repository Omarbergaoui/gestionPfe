package com.Application.Gestion.des.PFE.pfe;

import com.Application.Gestion.des.PFE.planning.Planning;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PfeRepository extends MongoRepository<PFE,String> {
    List<PFE> findByPlanningid(Planning planningid);
    PFE findFirstByPlanningidOrderByDateheureAsc(Planning planningid);
    Optional<PFE> findByPlanningidAndEtudiantemail(Planning planningid, String etudiantemail);
    List<PFE> findByDateheureBetween(LocalDateTime start, LocalDateTime end);
    List<PFE> findByDateheure(LocalDateTime localDateTime);
    @Query("{ '$or': [ {'encadreur.id': ?0}, {'president.id': ?0}, {'rapporteur.id': ?0} ] }")
    List<PFE> findAllByEnseignantParticipation(String enseignantId);
}
