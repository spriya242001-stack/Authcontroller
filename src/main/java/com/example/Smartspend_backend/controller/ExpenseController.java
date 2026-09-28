package com.example.Smartspend_backend.controller;

import com.example.Smartspend_backend.dto.ExpenseDTO;
import com.example.Smartspend_backend.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*") // Allows React frontend requests
public class ExpenseController {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // 1. Get all expenses for current logged-in user
    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getAllExpenses(Authentication authentication) {
        List<ExpenseDTO> expenses = expenseService.getUserExpenses(authentication.getName());
        return ResponseEntity.ok(expenses);
    }

    // 2. Get single expense by ID
    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id,
                                                     Authentication authentication) {
        ExpenseDTO expense = expenseService.getExpenseById(id, authentication.getName());
        return ResponseEntity.ok(expense);
    }

    // 3. Create a new expense (Triggers budget alert check automatically)
    @PostMapping
    public ResponseEntity<ExpenseDTO> createExpense(@Valid @RequestBody ExpenseDTO expenseDTO,
                                                    Authentication authentication) {
        ExpenseDTO createdExpense = expenseService.createExpense(expenseDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdExpense);
    }

    // 4. Update an existing expense
    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long id,
                                                    @Valid @RequestBody ExpenseDTO expenseDTO,
                                                    Authentication authentication) {
        ExpenseDTO updatedExpense = expenseService.updateExpense(id, expenseDTO, authentication.getName());
        return ResponseEntity.ok(updatedExpense);
    }

    // 5. Delete an expense by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id,
                                              Authentication authentication) {
        expenseService.deleteExpense(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    // 6. Filter expenses by date range or category
    @GetMapping("/filter")
    public ResponseEntity<List<ExpenseDTO>> filterExpenses(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String category,
            Authentication authentication) {

        List<ExpenseDTO> filtered = expenseService.filterExpenses(startDate, endDate, category, authentication.getName());
        return ResponseEntity.ok(filtered);
    }
}