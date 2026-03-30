package com.smartsure.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private JwtAuthenticationFilter.Config config;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() throws Exception {
        filter = new JwtAuthenticationFilter();
        // Use reflection to set the private jwtSecret field
        java.lang.reflect.Field field = JwtAuthenticationFilter.class.getDeclaredField("jwtSecret");
        field.setAccessible(true);
        field.set(filter, "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437");
        
        config = new JwtAuthenticationFilter.Config();
        chain = Mockito.mock(GatewayFilterChain.class);
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Test
    void testPublicPath_ShouldSkipFilter() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/auth/login").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.apply(config).filter(exchange, chain))
                .verifyComplete();
        
        Mockito.verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    void testSecuredPath_MissingHeader_ShouldReturn401() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/admin/actions").build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.apply(config).filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }
}
