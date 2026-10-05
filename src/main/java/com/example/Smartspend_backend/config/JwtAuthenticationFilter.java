package com.example.Smartspend_backend.config;

import com.example.Smartspend_backend.model.User;
import com.example.Smartspend_backend.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public JwtAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Skip JWT filter for all public routes
        return uri.startsWith("/api/auth/") ||
                uri.equals("/login") ||
                uri.equals("/register") ||
                uri.equals("/auth/register") ||
                uri.equals("/verify") ||
                uri.equals("/forgot-password") ||
                uri.equals("/reset-password") ||
                uri.equals("/reset-password.html") ||
                uri.equals("/verify.html") ||
                uri.startsWith("/css/") ||
                uri.startsWith("/js/") ||
                uri.startsWith("/static/") ||
                uri.startsWith("/images/") ||
                uri.startsWith("/webjars/");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // A password reset or a later login revokes the token in existing browser sessions.
        var existing = SecurityContextHolder.getContext().getAuthentication();
        if (existing != null && existing.getPrincipal() instanceof User sessionUser) {
            Optional<User> current = userRepository.findByEmail(sessionUser.getEmail());
            if (current.isEmpty() || sessionUser.getToken() == null
                    || !sessionUser.getToken().equals(current.get().getToken())) {
                SecurityContextHolder.clearContext();
                var session = request.getSession(false);
                if (session != null) session.invalidate();
            }
        }

        // 1. Extract the Authorization header
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // 2. Look up the user by the UUID token in the database
            Optional<User> userOptional = userRepository.findByToken(token);

            if (userOptional.isPresent()) {
                User user = userOptional.get();

                // 3. Authenticate the user in Spring Security context
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
