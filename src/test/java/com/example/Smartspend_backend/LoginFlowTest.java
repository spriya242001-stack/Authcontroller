package com.example.Smartspend_backend;

import com.example.Smartspend_backend.controller.AuthController;
import com.example.Smartspend_backend.controller.PageController;
import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.repository.UserRepository;
import com.example.Smartspend_backend.security.CustomUserDetailsService;
import com.example.Smartspend_backend.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class LoginFlowTest {
    private MockMvc mvc;
    private UserRepository repository;
    private User user;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        var encoder = new BCryptPasswordEncoder();
        user = new User("login@example.com", encoder.encode("correct-password"), "USER");
        when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(new CustomUserDetailsService(repository));
        provider.setPasswordEncoder(encoder);
        var service = new AuthService();
        ReflectionTestUtils.setField(service, "userRepository", repository);
        ReflectionTestUtils.setField(service, "passwordEncoder", encoder);
        ReflectionTestUtils.setField(service, "authenticationManager", new ProviderManager(provider));
        ReflectionTestUtils.setField(service, "verificationTokens", mock(com.example.Smartspend_backend.repository.VerificationTokenRepository.class));
        ReflectionTestUtils.setField(service, "emailService", mock(com.example.Smartspend_backend.service.EmailService.class));
        ReflectionTestUtils.setField(service, "verificationEmailEnabled", false);
        var controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", service);
        mvc = MockMvcBuilders.standaloneSetup(controller, new PageController()).build();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginPageForwardsToStaticHtml() throws Exception {
        mvc.perform(get("/login")).andExpect(forwardedUrl("/login.html"));
    }

    @Test
    void validLoginReturnsStoredTokenAndCreatesBrowserSession() throws Exception {
        var previousSession = new MockHttpSession();
        String previousId = previousSession.getId();
        var result = mvc.perform(post("/api/auth/login").session(previousSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"login@example.com\",\"password\":\"correct-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andReturn();
        assertNotNull(user.getToken());
        verify(repository).save(user);
        var session = result.getRequest().getSession(false);
        assertNotNull(session);
        assertNotEquals(previousId, session.getId());
        var context = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertTrue(context.getAuthentication().isAuthenticated());
        assertSame(user, context.getAuthentication().getPrincipal());
    }

    @Test
    void incorrectPasswordDoesNotCreateTokenOrSession() throws Exception {
        var result = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"login@example.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password")).andReturn();
        verify(repository, never()).save(any());
        assertNull(result.getRequest().getSession(false));
    }

    @Test
    void missingCredentialsAreRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(repository);
    }

    @Test
    void unknownEmailReceivesGenericRecoveryResponse() throws Exception {
        mvc.perform(post("/api/auth/forgot-password").param("email", "unknown@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("If an account exists, a password reset link has been sent."));
    }

    @Test
    void resetPasswordWithoutTokenCannotChangePassword() throws Exception {
        mvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"login@example.com\",\"newPassword\":\"replacement\"}"))
                .andExpect(status().isBadRequest());
        verify(repository, never()).save(any());
    }

    @Test
    void invalidVerificationCodeCannotReportSuccess() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/auth/verify")
                .param("code", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrationStillReturnsToken() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"new@example.com\",\"password\":\"correct-password\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
}
