package com.example.Smartspend_backend;

import com.example.Smartspend_backend.dto.BudgetDTO;
import com.example.Smartspend_backend.model.Budget;
import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.repository.BudgetRepository;
import com.example.Smartspend_backend.repository.ExpenseRepository;
import com.example.Smartspend_backend.repository.UserRepository;
import com.example.Smartspend_backend.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BudgetTrackingTest {
    @Test void totalsUseTheOwnerCategoryAndBudgetPeriod() {
        var user = new User("budget@example.com", "encoded", "USER");
        var budget = new Budget(1L, "Food", new BigDecimal("100.00"), 10, 2026, user);
        var users = mock(UserRepository.class);
        var budgets = mock(BudgetRepository.class);
        var expenses = mock(ExpenseRepository.class);
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(budgets.findByUser(user)).thenReturn(List.of(budget));
        var service = new BudgetService();
        ReflectionTestUtils.setField(service, "userRepository", users);
        ReflectionTestUtils.setField(service, "budgetRepository", budgets);
        ReflectionTestUtils.setField(service, "expenseRepository", expenses);
        for (String spent : List.of("25.50", "150.00")) {
            when(expenses.calculateTotalSpendByCategoryAndMonth(user.getEmail(), "Food", 10, 2026))
                    .thenReturn(new BigDecimal(spent));
            BudgetDTO dto = service.getUserBudgets(user.getEmail()).getFirst();
            assertEquals(new BigDecimal("100.00"), dto.getLimitAmount());
            assertEquals(new BigDecimal(spent), dto.getSpentAmount());
            assertEquals(new BigDecimal("100.00").subtract(new BigDecimal(spent)), dto.getRemainingAmount());
            assertEquals(new BigDecimal(spent), dto.getPercentage());
        }
        when(expenses.calculateTotalSpendByCategoryAndMonth(user.getEmail(), "Food", 10, 2026)).thenReturn(null);
        var dto = service.getUserBudgets(user.getEmail()).getFirst();
        assertEquals(BigDecimal.ZERO, dto.getSpentAmount());
        assertEquals(new BigDecimal("100.00"), dto.getRemainingAmount());
        assertEquals(new BigDecimal("0.00"), dto.getPercentage());
    }
}
