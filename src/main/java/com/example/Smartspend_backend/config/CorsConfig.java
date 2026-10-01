package com.example.Smartspend_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter; // <-- This is the import you need

import java.util.Arrays;


@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Explicitly allow your Netlify app and local development
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "https://*.netlify.app",
                "https://incredible-toffee-ecf6f6.netlify.app"
        ));

        // Allowed HTTP methods
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Allowed headers
        corsConfiguration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));

        // Allow sending credentials
        corsConfiguration.setAllowCredentials(true);

        // Max age for preflight options requests
        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}