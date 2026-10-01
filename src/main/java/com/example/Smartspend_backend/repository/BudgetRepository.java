package com.example.Smartspend_backend.repository;

import com.example.Smartspend_backend.model.Budget;
import com.example.Smartspend_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@SuppressWarnings("unused")
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // 1. Find all budgets associated with a specific user
    List<Budget> findByUser(User user);

    // 2. Find a specific budget by user, category, month (int), and year (int)
    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.category = :category AND b.month = :month AND b.year = :year")
    Optional<Budget> findByUserAndCategoryAndMonthAndYear(
            @Param("user") User user,
            @Param("category") String category,
            @Param("month") int month,
            @Param("year") int year
    );
}