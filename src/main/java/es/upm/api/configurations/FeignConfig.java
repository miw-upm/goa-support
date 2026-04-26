package es.upm.api.configurations;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    private final TokenManager tokenManager;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtAuth && authentication.isAuthenticated()) {
                String tokenValue = jwtAuth.getToken().getTokenValue();
                template.header("Authorization", "Bearer " + tokenValue);
            } else {
                template.header("Authorization", "Bearer " + tokenManager.getToken());
            }
        };
    }
}
