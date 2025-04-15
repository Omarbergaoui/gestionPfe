package com.Application.Gestion.des.PFE.token;

import com.Application.Gestion.des.PFE.user.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends MongoRepository<Token, String> {
    Optional<Token> findValidByUser(String id);

    List<Token> findByUser(UserEntity user);

    List<Token> findByUserAndExpiredFalseAndRevokedFalse(String id);

    Optional<Token> findByToken(String refreshToken);
}
