package com.example.Smartspend_backend.repository;

import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

@Repository
@SuppressWarnings("unused")
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationToken> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<VerificationToken> findByUser(User user);
}
