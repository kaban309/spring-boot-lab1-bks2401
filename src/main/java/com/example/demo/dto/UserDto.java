package com.example.demo.dto;

import com.example.demo.model.User;

public final class UserDto {
    private UserDto() {}

    public record Request(String name, String email) {}

    public record Response(Long id, String name, String email) {
        public static Response from(User u) {
            return new Response(u.getId(), u.getName(), u.getEmail());
        }
    }
}
