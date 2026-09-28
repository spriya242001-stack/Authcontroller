package com.example.Smartspend_backend.controller;

import com.example.Smartspend_backend.dto.BudgetDTO;
import com.example.Smartspend_backend.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    // Line 25: Save budget for authenticated user
    @PostMapping
    public ResponseEntity<BudgetDTO> createOrUpdateBudget(@Valid @RequestBody BudgetDTO budgetDTO,
                                                          Authentication authentication) {
        BudgetDTO savedBudget = budgetService.createBudget(budgetDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBudget);
    }

    // Line 32: Get budgets by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BudgetDTO>> getBudgetsByUser(@PathVariable Long userId) {
        List<BudgetDTO> budgets = budgetService.getBudgetsByUserId(userId);
        return ResponseEntity.ok(budgets);
    }

    // Line 39: Get budgets for current logged-in user
    @GetMapping("/me")
    public ResponseEntity<List<BudgetDTO>> getCurrentUserBudgets(Authentication authentication) {
        List<BudgetDTO> budgets = budgetService.getUserBudgets(authentication.getName());
        return ResponseEntity.ok(budgets);
    }
}