package com.deptflow.api.auth;

import java.util.UUID;

/** Auth endpoint request/response contracts. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(String username, String email, String password) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record UserResponse(UUID id, String username, String email) {
    }

    public record TokenResponse(String token, UUID institutionId) {
    }
}
