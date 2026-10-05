package com.example.Smartspend_backend;

import com.example.Smartspend_backend.repository.UserRepository;
import com.example.Smartspend_backend.repository.VerificationTokenRepository;
import com.example.Smartspend_backend.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "app.verification-email-enabled=true")
@AutoConfigureMockMvc
@Transactional
class AuthRecoveryIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired VerificationTokenRepository tokens;
    @MockBean EmailService emailService;
    String email;
    final String password = "Original-password-123";

    String body(Object value) throws Exception { return json.writeValueAsString(value); }

    @BeforeEach void register() throws Exception {
        email = "recovery-" + UUID.randomUUID() + "@example.invalid";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "password", password))))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.userId").isNumber());
    }

    String verificationCode() {
        var code = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(eq(email), code.capture());
        return code.getValue();
    }

    String resetToken() throws Exception {
        mvc.perform(post("/api/auth/forgot-password").param("email", email))
                .andExpect(status().isOk());
        var raw = ArgumentCaptor.forClass(String.class);
        verify(emailService, atLeastOnce()).sendPasswordResetEmail(eq(email), raw.capture());
        return raw.getValue();
    }

    @Test void verificationStoresHashAndConsumesSingleUseCode() throws Exception {
        String raw = verificationCode();
        var user = users.findByEmail(email).orElseThrow();
        assertNotEquals(raw, tokens.findByUser(user).orElseThrow().getToken());
        mvc.perform(get("/api/auth/verify").param("code", raw)).andExpect(status().isOk());
        assertEquals(true, users.findByEmail(email).orElseThrow().getEmailVerified());
        mvc.perform(get("/api/auth/verify").param("code", raw)).andExpect(status().isBadRequest());
    }

    @Test void resetRevokesOldBearerAndSessionAndRejectsReplay() throws Exception {
        var login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "password", password))))
                .andExpect(status().isOk()).andReturn();
        String bearer = json.readTree(login.getResponse().getContentAsString()).get("token").asText();
        var session = (MockHttpSession) login.getRequest().getSession(false);
        String raw = resetToken();
        String request = body(Map.of("email", email, "token", raw, "newPassword", "Replacement-password-456"));
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isOk());
        mvc.perform(get("/api/expenses").header("Authorization", "Bearer " + bearer)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/expenses").session(session)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "password", password)))).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "password", "Replacement-password-456")))).andExpect(status().isOk());
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(request))
                .andExpect(status().isBadRequest());
    }

    @Test void expiredTokenCannotChangePassword() throws Exception {
        String raw = resetToken();
        var record = tokens.findByUser(users.findByEmail(email).orElseThrow()).orElseThrow();
        record.setExpiryDate(LocalDateTime.now().minusSeconds(1));
        tokens.saveAndFlush(record);
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "token", raw, "newPassword", "replacement-password"))))
                .andExpect(status().isBadRequest());
    }

    @Test void verificationCodeCannotResetPassword() throws Exception {
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "token", verificationCode(), "newPassword", "replacement-password"))))
                .andExpect(status().isBadRequest());
    }

    @Test void wrongEmailCannotConsumeAnotherUsersResetLink() throws Exception {
        String raw = resetToken();
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", "other@example.invalid", "token", raw, "newPassword", "replacement-password"))))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "token", raw, "newPassword", "replacement-password"))))
                .andExpect(status().isOk());
    }

    @Test void reissuingRecoveryInvalidatesPreviousLink() throws Exception {
        String first = resetToken(), second = resetToken();
        assertNotEquals(first, second);
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "token", first, "newPassword", "replacement-password"))))
                .andExpect(status().isBadRequest());
    }

    @Test void recoveryDoesNotRevealWhetherAccountExists() throws Exception {
        mvc.perform(post("/api/auth/forgot-password").param("email", "missing@example.invalid"))
                .andExpect(status().isOk())
                .andExpect(content().string("If an account exists, a password reset link has been sent."));
        verify(emailService, never()).sendPasswordResetEmail(eq("missing@example.invalid"), anyString());
    }

    @Test void authenticatedExpenseBudgetAndReportFlowsPersistAndLogoutRevokesToken() throws Exception {
        var login = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("email", email, "password", password)))).andExpect(status().isOk()).andReturn();
        String token = json.readTree(login.getResponse().getContentAsString()).get("token").asText();
        long userId = users.findByEmail(email).orElseThrow().getId();
        String auth = "Bearer " + token;
        mvc.perform(post("/api/budgets").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("category", "Test", "amount", 100, "month", 10, "year", 2026))))
                .andExpect(status().isCreated());
        var expense = Map.of("title", "Deployment test", "amount", 10, "category", "Test", "type", "EXPENSE", "date", "2026-10-05");
        var created = mvc.perform(post("/api/expenses").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content(body(expense))).andExpect(status().isCreated()).andReturn();
        long id = json.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        mvc.perform(get("/api/expenses/" + id).header("Authorization", auth)).andExpect(status().isOk());
        mvc.perform(get("/api/expenses/filter").param("category", "Test").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(put("/api/expenses/" + id).header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                .content(body(Map.of("title", "Updated test", "amount", 25, "category", "Test", "type", "EXPENSE", "date", "2026-10-05"))))
                .andExpect(status().isOk());
        mvc.perform(get("/api/budgets/me").header("Authorization", auth)).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].spentAmount").value(25));
        mvc.perform(get("/api/budgets/user/" + userId).header("Authorization", auth)).andExpect(status().isOk());
        var pdf = mvc.perform(get("/api/reports/pdf").param("userId", "" + userId).header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(content().contentType("application/pdf")).andReturn().getResponse().getContentAsByteArray();
        assertTrue(new String(pdf, 0, 4, java.nio.charset.StandardCharsets.US_ASCII).equals("%PDF"));
        var excel = mvc.perform(get("/api/reports/excel").param("userId", "" + userId).header("Authorization", auth))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(excel))) {
            assertEquals("Updated test", workbook.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
        }
        mvc.perform(delete("/api/expenses/" + id).header("Authorization", auth)).andExpect(status().isNoContent());
        mvc.perform(get("/api/expenses").header("Authorization", auth)).andExpect(content().json("[]"));
        mvc.perform(post("/logout").header("Authorization", auth)).andExpect(status().isNoContent());
        mvc.perform(get("/api/expenses").header("Authorization", auth)).andExpect(status().isUnauthorized());
    }
}
