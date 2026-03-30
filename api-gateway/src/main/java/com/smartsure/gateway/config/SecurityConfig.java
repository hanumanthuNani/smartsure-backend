package com.smartsure.gateway.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity    
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint((exchange, e) -> {
                    exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
                    return reactor.core.publisher.Mono.empty();
                })
            )

            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/*/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/aggregate/**",
                    "/actuator/**",
                    "/api/auth/**",
                    "/auth/**",
                    "/favicon.ico",
                    "/error"
                ).permitAll()
                .anyExchange().permitAll()
            )
            .build();
    }
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // ✅ SECURITY FIX: Avoid using "*" in production. For demo, keep it but prevent credential leakage.
        config.setAllowedOriginPatterns(Arrays.asList("*")); 
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}