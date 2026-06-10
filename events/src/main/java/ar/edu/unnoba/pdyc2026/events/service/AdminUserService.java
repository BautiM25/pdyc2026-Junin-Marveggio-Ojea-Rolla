package ar.edu.unnoba.pdyc2026.events.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.edu.unnoba.pdyc2026.events.repository.UserRepository;
import jakarta.ws.rs.core.Response;

@Service
public class AdminUserService {

    @Autowired
    private Keycloak keycloakAdminClient;

    @Autowired
    private UserRepository userRepository;

    private final String realm = "unnoba";

    public CompletableFuture<List<UserRepresentation>> getAllUsers() {
        return CompletableFuture.supplyAsync(() ->
            keycloakAdminClient.realm(realm).users().list()
        );
    }

    /**
     * Crea un usuario administrador y le asigna el rol 'admin' de Realm.
     */
    public CompletableFuture<Response> createUser(UserRepresentation userRepresentation) {
        return CompletableFuture.supplyAsync(() -> {
            // 1. Crear el usuario en Keycloak
            Response response = keycloakAdminClient.realm(realm).users().create(userRepresentation);
            
            // Si la creación fue exitosa (HTTP 201 Created)
            if (response.getStatus() == 201) {
                try {
                    // 2. Extraer el ID único del usuario desde la URL de la cabecera Location
                    String path = response.getLocation().getPath();
                    String userId = path.substring(path.lastIndexOf('/') + 1);

                    // 3. OBTENER EL ROL "admin" CORRECTAMENTE POR SU NOMBRE
                    // Usamos el método correcto del cliente para buscar por nombre de rol de Realm
                    RoleRepresentation realmRole = keycloakAdminClient.realm(realm)
                            .roles()
                            .get("admin")
                            .toRepresentation(); 

                    // 4. Asignar el rol al usuario recién creado en Keycloak
                    UserResource userResource = keycloakAdminClient.realm(realm).users().get(userId);
                    userResource.roles().realmLevel().add(Collections.singletonList(realmRole));
                    
                    // 5. GUARDAR EL USUARIO EN NUESTRA BASE DE DATOS LOCAL
                    ar.edu.unnoba.pdyc2026.events.model.User localUser = new ar.edu.unnoba.pdyc2026.events.model.User();
                    localUser.setUsername(userRepresentation.getUsername().toLowerCase());
                    localUser.setEmail(userRepresentation.getEmail().toLowerCase());
                    userRepository.save(localUser);

                } catch (Exception e) {
                    throw new RuntimeException("User created in Keycloak but failed roles or local DB persistence: " + e.getMessage(), e);
                }
            }
            return response;
        });
    }

    public CompletableFuture<Void> deleteUser(String userId) {
        return CompletableFuture.runAsync(() ->
            keycloakAdminClient.realm(realm).users().delete(userId)
        );
    }

    public CompletableFuture<UserRepresentation> getUser(String userId) {
        return CompletableFuture.supplyAsync(() ->
            keycloakAdminClient.realm(realm).users().get(userId).toRepresentation()
        );
    }
}