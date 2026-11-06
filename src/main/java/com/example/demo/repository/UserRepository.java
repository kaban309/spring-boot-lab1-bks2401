package com.example.demo.repository;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByRoleAndStatus(Role role, UserStatus status);
}
