package com.example.Smartspend_backend;

import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Ensure the test user exists in the test database before running the login test
        userRepository.findByEmail("spriya242001@gmail.com").ifPresentOrElse(
                user -> {
                    user.setPassword(passwordEncoder.encode("YourActualPassword"));
                    user.setRole("USER");
                    userRepository.save(user);
                },
                () -> {
                    User newUser = new User();
                    newUser.setEmail("spriya242001@gmail.com");
                    newUser.setPassword(passwordEncoder.encode("YourActualPassword"));
                    newUser.setRole("USER");
                    userRepository.save(newUser);
                }
        );
    }

    @Test
    public void testLoginEndpoint() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"spriya242001@gmail.com\",\"password\":\"YourActualPassword\"}"))
                .andDo(print()) // Prints the full request/response details to the terminal
                .andExpect(status().isOk());
    }
}