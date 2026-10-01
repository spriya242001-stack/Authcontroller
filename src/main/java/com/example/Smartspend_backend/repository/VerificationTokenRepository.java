package com.example.Smartspend_backend.repository;

import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
@SuppressWarnings("unused")
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    Optional<VerificationToken> findByUser(User user);
}