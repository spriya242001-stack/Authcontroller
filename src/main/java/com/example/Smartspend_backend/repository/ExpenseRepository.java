package com.example.Smartspend_backend.repository;

import com.example.Smartspend_backend.model.Expense;
import com.example.Smartspend_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUser(User user);

    @Query("SELECT SUM(e.amount) FROM Expense e WHERE e.user.email = :email AND e.category = :category AND MONTH(e.date) = :month AND YEAR(e.date) = :year AND e.type = 'EXPENSE'")
    BigDecimal calculateTotalSpendByCategoryAndMonth(
            @Param("email") String email,
            @Param("category") String category,
            @Param("month") int month,
            @Param("year") int year
    );
}