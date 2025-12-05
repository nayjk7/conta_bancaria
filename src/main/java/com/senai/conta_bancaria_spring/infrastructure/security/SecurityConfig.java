package com.senai.conta_bancaria_spring.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // Clientes
                        .requestMatchers(HttpMethod.POST, "/api/cliente").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/cliente").hasRole("ADMIN")
                        .requestMatchers("/api/cliente/cpf/**").hasAnyRole("ADMIN", "CLIENTE")

                        // Contas - Operações
                        .requestMatchers(HttpMethod.POST, "/api/conta/{numeroConta}/**").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.GET, "/api/conta/**").hasAnyRole("ADMIN", "CLIENTE")
                        .requestMatchers("/api/conta/**").hasRole("ADMIN")

                        // ADIÇÃO: Pagamentos (Apenas Clientes)
                        .requestMatchers("/api/pagamentos/**").hasRole("CLIENTE")

                        // ADIÇÃO: Taxas (Ver apenas, criar só admin - controlado no controller)
                        .requestMatchers(HttpMethod.GET, "/api/taxas").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/taxas").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .userDetailsService(usuarioDetailsService);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}