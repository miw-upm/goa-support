package es.upm.api.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static es.upm.api.infrastructure.resources.SystemResource.SYSTEM;
import static es.upm.api.infrastructure.resources.SystemResource.VERSION_BADGE;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableMethodSecurity
public class ResourceServerConfig {
    public static final String ROLES_NAME = "roles";
    public static final String ROLE_AUTHORITY_PREFIX = "ROLE_";
    public static final String COGNITO_GROUPS_NAME = "cognito:groups";

    @Bean
    @Order(1)
    public SecurityFilterChain systemEndpointsSecurityConfig(HttpSecurity http) throws Exception {
        return http
                .securityMatcher(SYSTEM + "/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SYSTEM, SYSTEM + VERSION_BADGE).permitAll()
                        .anyRequest().denyAll()
                )
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityConfig(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                        jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix(ROLE_AUTHORITY_PREFIX);
        grantedAuthoritiesConverter.setAuthoritiesClaimName(ROLES_NAME);

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            if (jwt.getClaim(ROLES_NAME) != null) { // standard Auth2
                return grantedAuthoritiesConverter.convert(jwt);
            }
            return Optional.ofNullable(jwt.getClaimAsStringList(COGNITO_GROUPS_NAME))// AWS cognito: group as scope
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(group -> new SimpleGrantedAuthority(ROLE_AUTHORITY_PREFIX + group))
                    .collect(Collectors.toList());
        });
        return jwtAuthenticationConverter;
    }

}
