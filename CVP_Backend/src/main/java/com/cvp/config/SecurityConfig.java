package com.cvp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${aws.cognito.jwkUrl}")
    private String jwkUrl;

    @Value("${aws.cognito.issuerUri}")
    private String issuerUri;

    // Public endpoints filter chain (no OAuth2)
    @Bean
    @Order(1) // Higher priority (lower order number)
    public SecurityFilterChain publicSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatchers(matchers -> matchers
                        .requestMatchers(
                                "/users/register",
                                "/users/login",
                                "/users/confirm",
                                "/organization/register",
                                "/organization/login"))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

   
    // Authenticated endpoints filter chain (with OAuth2)
    @Bean
    @Order(2)
    public SecurityFilterChain privateSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatchers(matchers -> matchers
                        .requestMatchers(
                                "/api/**", // Existing protected paths
                                "/secure-endpoint" // Add this endpoint explicitly
                        ))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkUrl)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .jwtProcessorCustomizer(processor -> {
                    processor.setJWTClaimsSetVerifier((claims, context) -> {
                        if (!issuerUri.equals(claims.getIssuer())) {
                            throw new IllegalArgumentException("Invalid issuer");
                        }
                    });
                })
                .build();
    }
}