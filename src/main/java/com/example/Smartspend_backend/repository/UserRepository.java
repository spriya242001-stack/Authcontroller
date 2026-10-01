package com.example.Smartspend_backend.repository;

import com.example.Smartspend_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@SuppressWarnings("unused")
public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by email for authentication, token loading, and password resets
    Optional<User> findByEmail(String email);

    Optional<User> findByToken(String token);

    // Check if an email is already registered during sign-up
    Boolean existsByEmail(String email);
}