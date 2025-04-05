package com.Application.Gestion.des.PFE.user;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserEntity,String> {
    UserEntity findByEmail(String username);
    UserEntity findByCode(String code);
    UserEntity findByActivationcode(String activationcode);
}
