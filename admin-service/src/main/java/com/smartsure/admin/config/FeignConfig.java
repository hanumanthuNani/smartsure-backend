package com.smartsure.admin.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        if (name.equalsIgnoreCase("X-User-Roles") ||
                            name.equalsIgnoreCase("X-User-Email") ||
                            name.equalsIgnoreCase("X-UserId") ||
                            name.equalsIgnoreCase("Authorization")) {
                            requestTemplate.header(name, request.getHeader(name));
                        }
                    }
                }
            }
        };
    }
}
