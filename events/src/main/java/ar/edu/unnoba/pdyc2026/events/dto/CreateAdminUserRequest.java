package ar.edu.unnoba.pdyc2026.events.dto;

import lombok.Data;

@Data
public class CreateAdminUserRequest {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
}