package com.example.Smartspend_backend.repository;

import com.example.Smartspend_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by email for authentication, JWT loading, and password resets
    Optional<User> findByEmail(String email);

    // Check if an email is already registered during sign-up
    Boolean existsByEmail(String email);
}