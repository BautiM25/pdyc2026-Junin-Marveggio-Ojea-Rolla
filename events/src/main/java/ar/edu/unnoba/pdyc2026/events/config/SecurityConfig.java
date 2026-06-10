package ar.edu.unnoba.pdyc2026.events.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // 1. Endpoints públicos (No requieren token)
                .requestMatchers(HttpMethod.GET, "/artists").permitAll()
                .requestMatchers(HttpMethod.GET, "/artists/*/events").permitAll()
                .requestMatchers(HttpMethod.GET, "/events").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/*").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                
                // 2. Endpoints de administración (Requieren rol ADMIN)
                // Toda tu estructura bajo /admin/ es gestionada por administradores
                .requestMatchers("/admin/**").hasRole("admin")
                
                // 3. Endpoints de usuarios finales (Cualquier usuario autenticado)
                // Toda la sección de '/me/**' que requiere token de usuario común
                .requestMatchers("/me/**").authenticated()
                
                // Bloqueo preventivo por defecto para cualquier otra ruta
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withIssuerLocation(issuerUri).build();
    }

    /**
     * Converter encargado de parsear los roles y establecer el nombre legible del usuario desde el JWT
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        
        // 1. Mantenemos tu extractor de roles personalizado
        converter.setJwtGrantedAuthoritiesConverter(this::extractResourceRoles);
        
        // 2. AGREGAR ESTA LÍNEA CLAVE:
        // Le dice a Spring que extraiga el 'preferred_username' de Keycloak como el Name de la autenticación
        converter.setPrincipalClaimName("preferred_username");
        
        return converter;
    }

    /**
     * Lee la estructura: "realm_access": { "roles": ["admin", "user"] }
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        
        if (realmAccess == null || realmAccess.get("roles") == null) {
            return Collections.emptyList();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");
        
        return roles.stream()
                // Convertimos el rol en formato de Spring (ej: "admin" -> "ROLE_admin")
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .collect(Collectors.toList());
    }
}