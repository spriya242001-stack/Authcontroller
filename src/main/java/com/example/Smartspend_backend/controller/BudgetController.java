package com.example.Smartspend_backend.controller;

import com.example.Smartspend_backend.dto.BudgetDTO;
import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.repository.UserRepository;
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

    @Autowired
    private UserRepository userRepository;

    // Line 25: Save budget for authenticated user
    @PostMapping
    public ResponseEntity<BudgetDTO> createOrUpdateBudget(@Valid @RequestBody BudgetDTO budgetDTO,
                                                          Authentication authentication) {
        BudgetDTO savedBudget = budgetService.createBudget(budgetDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBudget);
    }

    // Line 37: Get budgets for current logged-in user (use this endpoint)
    @GetMapping("/me")
    public ResponseEntity<List<BudgetDTO>> getCurrentUserBudgets(Authentication authentication) {
        List<BudgetDTO> budgets = budgetService.getUserBudgets(authentication.getName());
        return ResponseEntity.ok(budgets);
    }

    // DEPRECATED: Use /me endpoint instead for better security
    // This endpoint requires proper authorization - only admin or user accessing own data
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BudgetDTO>> getBudgetsByUser(@PathVariable Long userId, Authentication authentication) {
        User requestingUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Security: Allow users to only access their own budgets, or admin can access any
        if (!requestingUser.getId().equals(userId) && !"ADMIN".equals(requestingUser.getRole())) {
            throw new RuntimeException("Unauthorized: You can only access your own budgets");
        }
        
        List<BudgetDTO> budgets = budgetService.getBudgetsByUserId(userId);
        return ResponseEntity.ok(budgets);
    }
}