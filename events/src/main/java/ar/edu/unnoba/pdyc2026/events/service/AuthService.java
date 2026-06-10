package ar.edu.unnoba.pdyc2026.events.service;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.unnoba.pdyc2026.events.model.User;
import ar.edu.unnoba.pdyc2026.events.repository.UserRepository;
import jakarta.ws.rs.core.Response;

@Service
public class AuthService {

    @Autowired
    private Keycloak keycloakAdminClient;

    @Autowired
    private UserRepository userRepository;

    private final String realm = "unnoba";

    public CompletableFuture<Boolean> registerUser(ar.edu.unnoba.pdyc2026.events.dto.RegisterRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. Configurar la representación del usuario para Keycloak
            UserRepresentation keycloakUser = new UserRepresentation();
            keycloakUser.setUsername(request.getUsername());
            keycloakUser.setEmail(request.getEmail());
            keycloakUser.setFirstName(request.getFirstName());
            keycloakUser.setLastName(request.getLastName());
            keycloakUser.setEnabled(true);

            // Configurar password
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getPassword());
            credential.setTemporary(false);
            keycloakUser.setCredentials(Collections.singletonList(credential));

            // 2. Enviar la petición de creación a Keycloak
            Response response = keycloakAdminClient.realm(realm).users().create(keycloakUser);

            // 3. Si Keycloak lo creó con éxito, guardamos en la base de datos local
            if (response.getStatus() == 201) {
                User localUser = new User();
                localUser.setUsername(request.getUsername().toLowerCase()); // <--- FORZAR MINÚSCULAS
                localUser.setEmail(request.getEmail().toLowerCase());       // <--- FORZAR MINÚSCULAS
                
                userRepository.save(localUser);
                return true;
            }
            
            return false;
        });
    }
}