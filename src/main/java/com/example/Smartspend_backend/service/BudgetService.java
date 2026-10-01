package com.example.Smartspend_backend.service;

import com.example.Smartspend_backend.dto.BudgetDTO;
import com.example.Smartspend_backend.model.Budget;
import com.example.Smartspend_backend.model.Expense;
import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.repository.BudgetRepository;
import com.example.Smartspend_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Create budget accepting budgetDTO and user email
    public BudgetDTO createBudget(BudgetDTO budgetDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        Budget budget = convertToEntity(budgetDTO);
        budget.setUser(user);

        Budget savedBudget = budgetRepository.save(budget);
        return convertToDTO(savedBudget);
    }

    // Overload for single-parameter call if used elsewhere
    @SuppressWarnings("unused")
    public BudgetDTO createBudget(BudgetDTO budgetDTO) {
        Budget budget = convertToEntity(budgetDTO);
        Budget savedBudget = budgetRepository.save(budget);
        return convertToDTO(savedBudget);
    }

    // 2. Get budgets accepting Long userId
    public List<BudgetDTO> getBudgetsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        return budgetRepository.findByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 3. Get budgets accepting String userEmail
    public List<BudgetDTO> getUserBudgets(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));

        return budgetRepository.findByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Overload if called elsewhere with User object
    @SuppressWarnings("unused")
    public List<Budget> getUserBudgets(User user) {
        return budgetRepository.findByUser(user);
    }

    // DTO Helper Mappings
    private BudgetDTO convertToDTO(Budget budget) {
        BudgetDTO dto = new BudgetDTO();
        dto.setId(budget.getId());
        dto.setCategory(budget.getCategory());
        dto.setAmount(budget.getAmount());
        dto.setMonth(budget.getMonth());
        dto.setYear(budget.getYear());
        return dto;
    }

    private Budget convertToEntity(BudgetDTO dto) {
        Budget budget = new Budget();
        budget.setCategory(dto.getCategory());
        budget.setAmount(dto.getAmount());
        budget.setMonth(dto.getMonth());
        budget.setYear(dto.getYear());
        return budget;
    }

    @SuppressWarnings("unused")
    public void checkBudgetAndNotify(Expense expense) {
        // TODO: Implement budget validation and notification logic once service is wired up
    }
}