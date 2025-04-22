package com.Application.Gestion.des.PFE.user;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity,String> {
    Optional<UserEntity> findByEmail(String username);
    UserEntity findByCode(String code);
    UserEntity findByActivationcode(String activationcode);
}
