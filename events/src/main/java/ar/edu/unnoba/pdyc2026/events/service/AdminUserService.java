package ar.edu.unnoba.pdyc2026.events.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.ws.rs.core.Response;

@Service
public class AdminUserService {

    @Autowired
    private Keycloak keycloakAdminClient;

    private final String realm = "unnoba";

    public CompletableFuture<List<UserRepresentation>> getAllUsers() {
        return CompletableFuture.supplyAsync(() ->
            keycloakAdminClient.realm(realm).users().list()
        );
    }

    public CompletableFuture<Response> createUser(UserRepresentation user) {
        return CompletableFuture.supplyAsync(() ->
            keycloakAdminClient.realm(realm).users().create(user)
        );
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