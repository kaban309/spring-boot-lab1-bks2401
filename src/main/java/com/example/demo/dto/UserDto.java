package com.example.demo.dto;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.UserStatus;

public final class UserDto {
    private UserDto() {}

    public record RegisterRequest(String name, String email, String password) {}

    public record Request(String name, String email, String password, Role role, UserStatus status) {}

    public record Response(Long id, String name, String email, Role role, UserStatus status) {
        public static Response from(User u) {
            return new Response(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getStatus());
        }
    }
}
