package com.codewithben.schoolmanagementsystem.Config;

import com.codewithben.schoolmanagementsystem.Utility.CustomAccessDeniedHandler;
import com.codewithben.schoolmanagementsystem.Utility.CustomAuthenticationEntryPoint;
import com.codewithben.schoolmanagementsystem.Utility.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityBean {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    // Comma-separated, from cors.allowed-origins (env: CORS_ALLOWED_ORIGINS).
    @Value("${cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(normalizeOrigins(allowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/finance/**").hasAnyAuthority("ACCOUNTANT", "PRINCIPAL")
                        .requestMatchers("/api/student/**").hasAnyAuthority("PRINCIPAL", "ADMINISTRATOR", "ACCOUNTANT", "TEACHING_STAFF")
                        .requestMatchers("/api/staff/**").hasAnyAuthority("ADMINISTRATOR", "TEACHING_STAFF", "PRINCIPAL", "ACCOUNTANT")
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ADMINISTRATOR", "PRINCIPAL", "ACCOUNTANT", "TEACHING_STAFF")
                        .requestMatchers("/api/offline/**").hasAnyAuthority("ADMINISTRATOR", "TEACHING_STAFF", "ACCOUNTANT")
                        .requestMatchers("/api/message/**").hasAnyAuthority("ADMINISTRATOR", "PRINCIPAL")
                        .anyRequest().authenticated()
                ).sessionManagement( session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ).addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                ).exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );

        return http.build();
    }

    /**
     * Browsers send the Origin header as scheme://host[:port] with no trailing
     * slash, and CORS matching is exact, so "https://app.example.com/" would never
     * match. Trim whitespace and trailing slashes, and drop empty entries.
     */
    static List<String> normalizeOrigins(String[] origins) {
        if (origins == null) {
            return List.of();
        }
        return Arrays.stream(origins)
                .map(String::trim)
                .map(origin -> origin.replaceAll("/+$", ""))
                .filter(origin -> !origin.isEmpty())
                .toList();
    }
}
