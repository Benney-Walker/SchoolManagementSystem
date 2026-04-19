package com.codewithben.schoolmanagementsystem.Config;

import com.codewithben.schoolmanagementsystem.Utility.CustomAccessDeniedHandler;
import com.codewithben.schoolmanagementsystem.Utility.CustomAuthenticationEntryPoint;
import com.codewithben.schoolmanagementsystem.Utility.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityBean {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    public SecurityBean(JwtAuthenticationFilter jwtAuthenticationFilter,
                        CustomAccessDeniedHandler customAccessDeniedHandler,
                        CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/finance/**").hasAnyAuthority("ACCOUNTANT", "PRINCIPAL")
                        .requestMatchers("/api/student/**").hasAnyAuthority("PRINCIPAL", "ADMINISTRATOR", "ACCOUNTANT", "TEACHING_STAFF")
                        .requestMatchers("/api/staff/**").hasAnyAuthority("ADMINISTRATOR", "TEACHING_STAFF", "PRINCIPAL", "ACCOUNTANT")
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ADMINISTRATOR", "PRINCIPAL", "ACCOUNTANT", "TEACHING_STAFF")
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
}
