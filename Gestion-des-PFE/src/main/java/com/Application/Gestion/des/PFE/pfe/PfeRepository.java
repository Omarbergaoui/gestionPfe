package com.Application.Gestion.des.PFE.pfe;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PfeRepository extends MongoRepository<PFE,String> {
    List<PFE> findByPlanningid(String Planningid);
    PFE findFirstByPlanningidOrderByDateheureAsc(String planningid);
    Optional<PFE> findByPlanningidAndEtudiantemail(String planningid, String etudiantemail);
    List<PFE> findByDateheureBetween(LocalDateTime start, LocalDateTime end);
    List<PFE> findByDateheure(LocalDateTime localDateTime);
}
