package com.example.Smartspend_backend.service;

import com.example.Smartspend_backend.dto.ExpenseDTO;
import com.example.Smartspend_backend.model.Expense;
import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.repository.ExpenseRepository;
import com.example.Smartspend_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetService budgetService;

    // 1. Get expense by ID with email verification
    public ExpenseDTO getExpenseById(Long id, String userEmail) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        if (!expense.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access to this expense");
        }
        return convertToDTO(expense);
    }

    // Overload for single-argument call if needed
    @SuppressWarnings("unused")
    public ExpenseDTO getExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
        return convertToDTO(expense);
    }

    // 2. Update expense with email verification
    public ExpenseDTO updateExpense(Long id, ExpenseDTO expenseDTO, String userEmail) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        if (!expense.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to update this expense");
        }

        expense.setTitle(expenseDTO.getTitle());
        expense.setAmount(expenseDTO.getAmount());
        expense.setCategory(expenseDTO.getCategory());
        expense.setType(expenseDTO.getType());
        expense.setDate(expenseDTO.getDate());
        expense.setDescription(expenseDTO.getDescription());

        Expense updatedExpense = expenseRepository.save(expense);

        // Re-check budget limits and notify if threshold exceeded
        budgetService.checkBudgetAndNotify(updatedExpense);

        return convertToDTO(updatedExpense);
    }

    // 3. Delete expense with email verification
    public void deleteExpense(Long id, String userEmail) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        if (!expense.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized to delete this expense");
        }

        expenseRepository.delete(expense);
    }

    // Overload for single-argument call if needed
    @SuppressWarnings("unused")
    public void deleteExpense(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new RuntimeException("Expense not found with id: " + id);
        }
        expenseRepository.deleteById(id);
    }

    // 4. Filter expenses by date range and category
    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String category, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        List<Expense> expenses = expenseRepository.findByUser(user);

        return expenses.stream()
                .filter(e -> startDate == null || !e.getDate().isBefore(startDate))
                .filter(e -> endDate == null || !e.getDate().isAfter(endDate))
                .filter(e -> category == null || category.isEmpty() || e.getCategory().equalsIgnoreCase(category))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Get all user expenses
    public List<ExpenseDTO> getUserExpenses(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        return expenseRepository.findByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Create new expense
    public ExpenseDTO createExpense(ExpenseDTO expenseDTO, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found: " + userEmail));

        Expense expense = convertToEntity(expenseDTO);
        expense.setUser(user);

        Expense savedExpense = expenseRepository.save(expense);
        budgetService.checkBudgetAndNotify(savedExpense);

        return convertToDTO(savedExpense);
    }

    // Helper mapping methods
    private ExpenseDTO convertToDTO(Expense expense) {
        ExpenseDTO dto = new ExpenseDTO();
        dto.setId(expense.getId());
        dto.setTitle(expense.getTitle());
        dto.setAmount(expense.getAmount());
        dto.setCategory(expense.getCategory());
        dto.setType(expense.getType());
        dto.setDate(expense.getDate());
        dto.setDescription(expense.getDescription());
        return dto;
    }

    private Expense convertToEntity(ExpenseDTO dto) {
        Expense expense = new Expense();
        expense.setTitle(dto.getTitle());
        expense.setAmount(dto.getAmount());
        expense.setCategory(dto.getCategory());
        expense.setType(dto.getType());
        expense.setDate(dto.getDate());
        expense.setDescription(dto.getDescription());
        return expense;
    }
}