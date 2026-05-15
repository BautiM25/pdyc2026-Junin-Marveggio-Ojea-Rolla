package ar.edu.unnoba.pdyc2026.events.controller;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unnoba.pdyc2026.events.dto.CreateAdminUserRequest;
import ar.edu.unnoba.pdyc2026.events.service.AdminUserService;
import jakarta.ws.rs.core.Response;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<List<UserRepresentation>> getUsers() throws ExecutionException, InterruptedException {
        List<UserRepresentation> users = adminUserService.getAllUsers().get();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserRepresentation> getUser(@PathVariable String userId) throws ExecutionException, InterruptedException {
        UserRepresentation user = adminUserService.getUser(userId).get();
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateAdminUserRequest request) throws ExecutionException, InterruptedException {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        user.setCredentials(Collections.singletonList(credential));

        Response response = adminUserService.createUser(user).get();
        if (response.getStatus() == 201) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } else {
            return ResponseEntity.status(response.getStatus()).body("Error creating user");
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) throws ExecutionException, InterruptedException {
        adminUserService.deleteUser(userId).get();
        return ResponseEntity.noContent().build();
    }
}