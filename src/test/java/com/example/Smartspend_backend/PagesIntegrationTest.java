package com.example.Smartspend_backend;

import com.example.Smartspend_backend.config.JwtAuthenticationFilter;
import com.example.Smartspend_backend.config.SecurityConfig;
import com.example.Smartspend_backend.controller.PageController;
import com.example.Smartspend_backend.controller.ExpenseController;
import com.example.Smartspend_backend.controller.BudgetController;
import com.example.Smartspend_backend.dto.ExpenseDTO;
import com.example.Smartspend_backend.dto.BudgetDTO;
import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.repository.UserRepository;
import com.example.Smartspend_backend.security.CustomUserDetailsService;
import com.example.Smartspend_backend.service.ExpenseService;
import com.example.Smartspend_backend.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({PageController.class, ExpenseController.class, BudgetController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, CustomUserDetailsService.class})
class PagesIntegrationTest {
    @Autowired MockMvc mvc;
    @MockBean UserRepository users;
    @MockBean ExpenseService expenses;
    @MockBean BudgetService budgets;

    private MockHttpSession session() {
        var user = new User("pages@example.com", "encoded", "USER");
        user.setToken("session-token");
        when(users.findByEmail("pages@example.com")).thenReturn(java.util.Optional.of(user));
        var context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        var session = new MockHttpSession();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        return session;
    }

    @Test void pagesForwardToStaticHtmlAndResourcesExist() throws Exception {
        for (String page : List.of("dashboard", "expenses", "budgets")) {
            mvc.perform(get("/" + page).session(session())).andExpect(forwardedUrl("/" + page + ".html"));
            mvc.perform(get("/" + page + ".html").session(session())).andExpect(status().isOk());
        }
        mvc.perform(get("/forgot-password")).andExpect(forwardedUrl("/forgot-password.html"));
        mvc.perform(get("/forgot-password.html")).andExpect(status().isOk());
        mvc.perform(get("/css/style.css")).andExpect(status().isOk());
        for (String script : List.of("common", "dashboard", "expenses", "budgets", "forgot-password")) {
            mvc.perform(get("/js/" + script + ".js")).andExpect(status().isOk());
        }
    }

    @Test void unauthenticatedPagesRedirectAndApiRejects() throws Exception {
        mvc.perform(get("/dashboard")).andExpect(status().isFound()).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/api/expenses")).andExpect(status().isUnauthorized());
    }

    @Test void sessionIdentifiesExpenseOwnerForCrud() throws Exception {
        when(expenses.getUserExpenses("pages@example.com")).thenReturn(List.of());
        mvc.perform(get("/api/expenses").session(session())).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(post("/api/expenses").session(session()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Lunch\",\"amount\":10,\"category\":\"Food\",\"type\":\"EXPENSE\",\"date\":\"2026-10-03\"}"))
                .andExpect(status().isCreated());
        verify(expenses).createExpense(any(ExpenseDTO.class), eq("pages@example.com"));
        mvc.perform(put("/api/expenses/1").session(session()).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Lunch\",\"amount\":15,\"category\":\"Food\",\"type\":\"EXPENSE\",\"date\":\"2026-10-03\"}"))
                .andExpect(status().isOk());
        verify(expenses).updateExpense(eq(1L), any(ExpenseDTO.class), eq("pages@example.com"));
        mvc.perform(delete("/api/expenses/1").session(session())).andExpect(status().isNoContent());
        verify(expenses).deleteExpense(1L, "pages@example.com");
    }

    @Test void budgetsUseAuthenticatedOwnerAndJsonFields() throws Exception {
        when(budgets.getUserBudgets("pages@example.com")).thenReturn(List.of());
        mvc.perform(get("/api/budgets/me").session(session())).andExpect(status().isOk());
        mvc.perform(post("/api/budgets").session(session()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"Food\",\"amount\":500,\"month\":10,\"year\":2026}"))
                .andExpect(status().isCreated());
        verify(budgets).createBudget(argThat(dto -> dto.getMonth() == 10 && dto.getYear() == 2026
                && dto.getAmount().intValue() == 500), eq("pages@example.com"));
    }

    @Test void invalidExpenseAndBudgetAreRejectedBeforeSaving() throws Exception {
        mvc.perform(post("/api/expenses").session(session()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Bad\",\"amount\":-1,\"category\":\"Food\",\"type\":\"INVALID\",\"date\":\"2026-10-03\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/budgets").session(session()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"Food\",\"amount\":-1,\"month\":13,\"year\":2026}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(expenses, budgets);
    }

    @Test void missingAndZeroBudgetMonthsHaveClearErrors() throws Exception {
        mvc.perform(post("/api/budgets").session(session()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"Food\",\"amount\":500,\"year\":2026}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.month").value("Budget month is required"));
        mvc.perform(post("/api/budgets").session(session()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"Food\",\"amount\":500,\"month\":0,\"year\":2026}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.month").value("Budget month must be between 1 and 12"));
        verifyNoInteractions(budgets);
    }

    @Test void logoutInvalidatesBrowserSession() throws Exception {
        var session = session();
        var user = new User("pages@example.com", "encoded", "USER");
        user.setToken("old-token");
        when(users.findByEmail("pages@example.com")).thenReturn(java.util.Optional.of(user));
        user.setToken("session-token");
        mvc.perform(post("/logout").session(session)).andExpect(status().isNoContent());
        org.junit.jupiter.api.Assertions.assertTrue(session.isInvalid());
        org.junit.jupiter.api.Assertions.assertNull(user.getToken());
        verify(users).save(user);
        mvc.perform(get("/dashboard")).andExpect(status().isFound())
                .andExpect(redirectedUrl("/login"));
    }
}
