package com.Application.Gestion.des.PFE.enseignant;

import com.Application.Gestion.des.PFE.departement.Departement;
import com.Application.Gestion.des.PFE.enumeration.Role;
import com.Application.Gestion.des.PFE.salle.Salle;
import org.springframework.data.mongodb.repository.MongoRepository;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnseignantRepository extends MongoRepository<Enseignant,String> {
    Enseignant findByEmail(String email);
    List<Enseignant> findByDepartementId(Departement departement);
    List<Enseignant> findByRoleInAndDisponibiliteNotContaining(List<Role> roles, LocalDateTime date);
    List<Enseignant> findByRoleInAndDisponibiliteContaining(List<Role> roles, LocalDateTime date);
    List<Enseignant> findByRole(String role);

}
