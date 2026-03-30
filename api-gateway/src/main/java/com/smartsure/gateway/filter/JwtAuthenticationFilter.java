package com.smartsure.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public JwtAuthenticationFilter() {
        super(Config.class);
    }

    public static class Config {
        // Empty static class
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            if (path.contains("/v3/api-docs") || 
                path.startsWith("/swagger-ui") || 
                path.startsWith("/swagger-resources") || 
                path.startsWith("/webjars") ||
                path.startsWith("/actuator") ||
                path.startsWith("/api/auth/register") || 
                path.startsWith("/api/auth/login") ||
                path.startsWith("/auth/")) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                
                // Roles is stored as a List in JWT, convert to comma-separated string
                Object rolesObj = claims.get("roles");
                String roles = "";
                if (rolesObj instanceof java.util.Collection) {
                    @SuppressWarnings("unchecked")
                    java.util.Collection<String> rolesList = (java.util.Collection<String>) rolesObj;
                    roles = String.join(",", rolesList);
                } else if (rolesObj != null) {
                    roles = rolesObj.toString().replace("[", "").replace("]", "").replace(" ", "");
                }

                // ✅ SECURITY FIX: Remove existing headers to prevent spoofing
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-User-Roles", roles)
                        .header("X-User-Email", claims.getSubject())
                        .header("X-UserId", String.valueOf(claims.get("userId")))
                        .headers(httpHeaders -> {
                            httpHeaders.remove("X-Forwarded-For"); // Optional: prevent IP spoofing
                        })
                        .build();
                
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (Exception ex) {
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }
   
}
