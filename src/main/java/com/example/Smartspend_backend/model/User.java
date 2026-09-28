package com.example.Smartspend_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*; // Correct JPA annotations package
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @jakarta.persistence.Id // Fixed: Using JPA Id instead of Spring Data Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude // Prevents infinite recursion during toString() calls
    @JsonIgnore       // Prevents infinite recursion during JSON serialization
    private List<Expense> expenses;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude // Prevents infinite recursion during toString() calls
    @JsonIgnore       // Prevents infinite recursion during JSON serialization
    private List<Budget> budgets;
}